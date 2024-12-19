package fetchingtask;

public interface Task {

	public void saveCheckpoint();

	public void loadCheckpoint();

	public void deleteCheckpoint();

	public void runTask();

	public String getOutputFilePath();
}
