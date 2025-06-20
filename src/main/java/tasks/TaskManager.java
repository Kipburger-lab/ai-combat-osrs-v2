package tasks;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import combat.CombatStyleManager;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Advanced task management system for automated progression
 * Handles task queuing, execution, and completion tracking
 * 
 * @author TraeAI
 * @version 1.0
 */
public class TaskManager {
    
    // Task queue and current task
    private final Queue<CombatTask> taskQueue;
    private CombatTask currentTask;
    private boolean isRunning;
    
    // Task execution tracking
    private long taskStartTime;
    private long totalTaskTime;
    private int completedTasks;
    
    // Dependencies
    private CombatStyleManager styleManager;
    
    /**
     * Represents a combat training task
     */
    public static class CombatTask {
        private final String taskId;
        private final String description;
        private final List<String> targetNames;
        private final List<Integer> targetIds;
        private final Skill targetSkill;
        private final int targetLevel;
        private final String combatStyle;
        private final Map<String, Object> parameters;
        private final long createdTime;
        
        // Task status
        private TaskStatus status;
        private int startLevel;
        private long startTime;
        private long completionTime;
        private String failureReason;
        
        public enum TaskStatus {
            PENDING,
            ACTIVE,
            COMPLETED,
            FAILED,
            CANCELLED
        }
        
        public CombatTask(String taskId, String description, List<String> targetNames, 
                         List<Integer> targetIds, Skill targetSkill, int targetLevel, 
                         String combatStyle) {
            this.taskId = taskId;
            this.description = description;
            this.targetNames = new ArrayList<>(targetNames != null ? targetNames : Collections.emptyList());
            this.targetIds = new ArrayList<>(targetIds != null ? targetIds : Collections.emptyList());
            this.targetSkill = targetSkill;
            this.targetLevel = targetLevel;
            this.combatStyle = combatStyle;
            this.parameters = new HashMap<>();
            this.createdTime = System.currentTimeMillis();
            this.status = TaskStatus.PENDING;
        }
        
        // Getters
        public String getTaskId() { return taskId; }
        public String getDescription() { return description; }
        public List<String> getTargetNames() { return new ArrayList<>(targetNames); }
        public List<Integer> getTargetIds() { return new ArrayList<>(targetIds); }
        public Skill getTargetSkill() { return targetSkill; }
        public int getTargetLevel() { return targetLevel; }
        public String getCombatStyle() { return combatStyle; }
        public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
        public TaskStatus getStatus() { return status; }
        public int getStartLevel() { return startLevel; }
        public long getStartTime() { return startTime; }
        public long getCompletionTime() { return completionTime; }
        public String getFailureReason() { return failureReason; }
        public long getCreatedTime() { return createdTime; }
        
        // Setters (package private)
        void setStatus(TaskStatus status) { this.status = status; }
        void setStartLevel(int startLevel) { this.startLevel = startLevel; }
        void setStartTime(long startTime) { this.startTime = startTime; }
        void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
        void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        // Parameter management
        public void setParameter(String key, Object value) {
            parameters.put(key, value);
        }
        
        public Object getParameter(String key) {
            return parameters.get(key);
        }
        
        public <T> T getParameter(String key, Class<T> type) {
            Object value = parameters.get(key);
            if (value != null && type.isInstance(value)) {
                return type.cast(value);
            }
            return null;
        }
        
        /**
         * Checks if the task is completed based on current skill level
         * 
         * @return true if task is completed, false otherwise
         */
        public boolean isCompleted() {
            if (targetSkill == null) {
                return false;
            }
            
            int currentLevel = Skills.getRealLevel(targetSkill);
            return currentLevel >= targetLevel;
        }
        
        /**
         * Gets the progress percentage of the task
         * 
         * @return progress percentage (0-100)
         */
        public double getProgressPercentage() {
            if (targetSkill == null || startLevel <= 0) {
                return 0.0;
            }
            
            int currentLevel = Skills.getRealLevel(targetSkill);
            if (currentLevel >= targetLevel) {
                return 100.0;
            }
            
            if (targetLevel <= startLevel) {
                return 100.0;
            }
            
            return ((double) (currentLevel - startLevel) / (targetLevel - startLevel)) * 100.0;
        }
        
        /**
         * Gets estimated time remaining based on current progress
         * 
         * @return estimated time remaining in milliseconds, or -1 if cannot estimate
         */
        public long getEstimatedTimeRemaining() {
            if (status != TaskStatus.ACTIVE || startTime <= 0) {
                return -1;
            }
            
            double progress = getProgressPercentage();
            if (progress <= 0 || progress >= 100) {
                return -1;
            }
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            return (long) ((elapsedTime / progress) * (100 - progress));
        }
        
        @Override
        public String toString() {
            return String.format("Task[%s]: %s (%s to %d) - %s", 
                               taskId, description, 
                               targetSkill != null ? targetSkill.getName() : "Unknown", 
                               targetLevel, status);
        }
    }
    
    public TaskManager() {
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.currentTask = null;
        this.isRunning = false;
        this.taskStartTime = 0;
        this.totalTaskTime = 0;
        this.completedTasks = 0;
        
        Logger.log("TaskManager initialized");
    }
    
    /**
     * Sets the combat style manager dependency
     * 
     * @param styleManager combat style manager instance
     */
    public void setStyleManager(CombatStyleManager styleManager) {
        this.styleManager = styleManager;
    }
    
    /**
     * Adds a new task to the queue
     * 
     * @param task task to add
     */
    public void addTask(CombatTask task) {
        if (task == null) {
            Logger.error("Cannot add null task");
            return;
        }
        
        taskQueue.offer(task);
        Logger.log("Added task: " + task.getDescription());
    }
    
    /**
     * Creates and adds a simple combat task
     * 
     * @param description task description
     * @param targetName NPC name to fight
     * @param skill skill to train
     * @param targetLevel target level to reach
     * @param combatStyle combat style to use
     */
    public void addSimpleTask(String description, String targetName, Skill skill, 
                             int targetLevel, String combatStyle) {
        String taskId = "task_" + System.currentTimeMillis();
        List<String> targetNames = targetName != null ? Arrays.asList(targetName) : Collections.emptyList();
        
        CombatTask task = new CombatTask(taskId, description, targetNames, 
                                       Collections.emptyList(), skill, targetLevel, combatStyle);
        addTask(task);
    }
    
    /**
     * Creates and adds a task with NPC ID
     * 
     * @param description task description
     * @param targetId NPC ID to fight
     * @param skill skill to train
     * @param targetLevel target level to reach
     * @param combatStyle combat style to use
     */
    public void addTaskWithId(String description, int targetId, Skill skill, 
                             int targetLevel, String combatStyle) {
        String taskId = "task_" + System.currentTimeMillis();
        List<Integer> targetIds = Arrays.asList(targetId);
        
        CombatTask task = new CombatTask(taskId, description, Collections.emptyList(), 
                                       targetIds, skill, targetLevel, combatStyle);
        addTask(task);
    }
    
    /**
     * Starts task execution
     */
    public void start() {
        if (isRunning) {
            Logger.log("TaskManager is already running");
            return;
        }
        
        isRunning = true;
        Logger.log("TaskManager started");
    }
    
    /**
     * Stops task execution
     */
    public void stop() {
        if (!isRunning) {
            Logger.log("TaskManager is not running");
            return;
        }
        
        isRunning = false;
        
        // Mark current task as cancelled if active
        if (currentTask != null && currentTask.getStatus() == CombatTask.TaskStatus.ACTIVE) {
            currentTask.setStatus(CombatTask.TaskStatus.CANCELLED);
            currentTask.setCompletionTime(System.currentTimeMillis());
        }
        
        Logger.log("TaskManager stopped");
    }
    
    /**
     * Updates task manager - should be called regularly
     * 
     * @return true if there's an active task, false otherwise
     */
    public boolean update() {
        if (!isRunning) {
            return false;
        }
        
        try {
            // Check if current task is completed or failed
            if (currentTask != null) {
                updateCurrentTask();
            }
            
            // Start next task if no current task
            if (currentTask == null || 
                currentTask.getStatus() == CombatTask.TaskStatus.COMPLETED ||
                currentTask.getStatus() == CombatTask.TaskStatus.FAILED ||
                currentTask.getStatus() == CombatTask.TaskStatus.CANCELLED) {
                
                startNextTask();
            }
            
            return currentTask != null && currentTask.getStatus() == CombatTask.TaskStatus.ACTIVE;
            
        } catch (Exception e) {
            Logger.error("Error in TaskManager update: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Updates the current task status
     */
    private void updateCurrentTask() {
        if (currentTask == null || currentTask.getStatus() != CombatTask.TaskStatus.ACTIVE) {
            return;
        }
        
        // Check if task is completed
        if (currentTask.isCompleted()) {
            completeCurrentTask();
            return;
        }
        
        // Update combat style if needed
        if (styleManager != null && currentTask.getCombatStyle() != null) {
            CombatStyleManager.CombatStyle desiredStyle = 
                styleManager.getStyleForSkill(currentTask.getCombatStyle());
            
            if (desiredStyle != null && desiredStyle != styleManager.getCurrentStyle()) {
                styleManager.setCombatStyle(desiredStyle);
            }
        }
    }
    
    /**
     * Completes the current task
     */
    private void completeCurrentTask() {
        if (currentTask == null) {
            return;
        }
        
        currentTask.setStatus(CombatTask.TaskStatus.COMPLETED);
        currentTask.setCompletionTime(System.currentTimeMillis());
        
        long taskDuration = currentTask.getCompletionTime() - currentTask.getStartTime();
        totalTaskTime += taskDuration;
        completedTasks++;
        
        Logger.log("Task completed: " + currentTask.getDescription() + 
                  " (Duration: " + (taskDuration / 1000) + "s)");
        
        currentTask = null;
    }
    
    /**
     * Starts the next task in the queue
     */
    private void startNextTask() {
        if (taskQueue.isEmpty()) {
            currentTask = null;
            return;
        }
        
        currentTask = taskQueue.poll();
        if (currentTask == null) {
            return;
        }
        
        // Initialize task
        currentTask.setStatus(CombatTask.TaskStatus.ACTIVE);
        currentTask.setStartTime(System.currentTimeMillis());
        
        if (currentTask.getTargetSkill() != null) {
            currentTask.setStartLevel(Skills.getRealLevel(currentTask.getTargetSkill()));
        }
        
        taskStartTime = System.currentTimeMillis();
        
        Logger.log("Started task: " + currentTask.getDescription());
        
        // Set combat style if specified
        if (styleManager != null && currentTask.getCombatStyle() != null) {
            CombatStyleManager.CombatStyle desiredStyle = 
                styleManager.getStyleForSkill(currentTask.getCombatStyle());
            
            if (desiredStyle != null) {
                styleManager.setCombatStyle(desiredStyle);
            }
        }
    }
    
    /**
     * Fails the current task with a reason
     * 
     * @param reason failure reason
     */
    public void failCurrentTask(String reason) {
        if (currentTask == null || currentTask.getStatus() != CombatTask.TaskStatus.ACTIVE) {
            return;
        }
        
        currentTask.setStatus(CombatTask.TaskStatus.FAILED);
        currentTask.setFailureReason(reason);
        currentTask.setCompletionTime(System.currentTimeMillis());
        
        Logger.error("Task failed: " + currentTask.getDescription() + " - " + reason);
        
        currentTask = null;
    }
    
    /**
     * Clears all tasks from the queue
     */
    public void clearTasks() {
        taskQueue.clear();
        Logger.log("All tasks cleared from queue");
    }
    
    /**
     * Gets the current active task
     * 
     * @return current task or null if none active
     */
    public CombatTask getCurrentTask() {
        return currentTask;
    }
    
    /**
     * Gets the number of tasks in the queue
     * 
     * @return number of queued tasks
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Gets all tasks in the queue
     * 
     * @return list of queued tasks
     */
    public List<CombatTask> getQueuedTasks() {
        return new ArrayList<>(taskQueue);
    }
    
    /**
     * Checks if task manager is running
     * 
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Gets the number of completed tasks
     * 
     * @return number of completed tasks
     */
    public int getCompletedTaskCount() {
        return completedTasks;
    }
    
    /**
     * Gets the total time spent on completed tasks
     * 
     * @return total task time in milliseconds
     */
    public long getTotalTaskTime() {
        return totalTaskTime;
    }
    
    /**
     * Gets the average time per completed task
     * 
     * @return average task time in milliseconds, or 0 if no completed tasks
     */
    public long getAverageTaskTime() {
        return completedTasks > 0 ? totalTaskTime / completedTasks : 0;
    }
    
    /**
     * Gets a summary of task manager status
     * 
     * @return status summary string
     */
    public String getStatusSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("TaskManager Status:\n");
        sb.append("Running: ").append(isRunning).append("\n");
        sb.append("Current Task: ").append(currentTask != null ? currentTask.getDescription() : "None").append("\n");
        sb.append("Queue Size: ").append(taskQueue.size()).append("\n");
        sb.append("Completed Tasks: ").append(completedTasks).append("\n");
        
        if (currentTask != null && currentTask.getStatus() == CombatTask.TaskStatus.ACTIVE) {
            sb.append("Current Progress: ").append(String.format("%.1f%%", currentTask.getProgressPercentage())).append("\n");
            
            long eta = currentTask.getEstimatedTimeRemaining();
            if (eta > 0) {
                sb.append("ETA: ").append(eta / 1000).append("s\n");
            }
        }
        
        return sb.toString();
    }
}