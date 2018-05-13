import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class EmgHotswapAgentAction extends AnAction {

  public EmgHotswapAgentAction() {
    super("EmgHotswap Agent");
  }

  @Override
  public void actionPerformed(AnActionEvent event) {

    Project project = event.getProject();

    VirtualFile selectedFile =
        (VirtualFile) event.getDataContext().getData(DataConstants.VIRTUAL_FILE);
    if (validateResourcesFolder(selectedFile)) {
      addDefaultConfigurationFile(selectedFile, project);
    } else {
      showErrorMessage("Not a resource folder selected", project);
    }
  }

  private void addDefaultConfigurationFile(VirtualFile selectedFile, Project project) {
    WriteCommandAction.runWriteCommandAction(
        project,
        new Runnable() {
          @Override
          public void run() {
            try {
              VirtualFile properties = getDefaultConfigFile();
              String fileContent = getDefaultContent(selectedFile);
              properties.setBinaryContent(fileContent.getBytes());

              FileEditorManager.getInstance(project).openFile(properties, true);
            } catch (IOException e1) {
              e1.printStackTrace();
            }
          }

          private String getDefaultContent(VirtualFile selectedFile) {
            String targetPath =
                selectedFile.getParent().getParent().getParent().getPath() + "/target/classes/";
            StringBuilder propertiesBuilder = new StringBuilder("# ADD -XXaltjvm=dcevm -javaagent:/Users/edison/java/hotswaphotswap-agent-1.2.1-SNAPSHOT.jar to JVM Options\n");
            propertiesBuilder.append("extraClasspath=").append(targetPath).append("\n");
            propertiesBuilder.append("watchResources=").append(targetPath).append("\n");
            propertiesBuilder.append("disabledPlugins=").append("Log4j2").append("\n");
            propertiesBuilder.append("autoHotswap=").append("true").append("\n");
            propertiesBuilder.append("LOGGER=").append("info").append("\n");
            return propertiesBuilder.toString();
          }

          @NotNull
          private VirtualFile getDefaultConfigFile() throws IOException {
            final String defaultPropertiesFile = "hotswap-agent.properties";
            for (VirtualFile virtualFile : selectedFile.getChildren()) {
              if (virtualFile.getName().equalsIgnoreCase(defaultPropertiesFile)) {
                return virtualFile;
              }
            }
            return selectedFile.createChildData(this, defaultPropertiesFile);
          }
        });
  }

  private boolean validateResourcesFolder(VirtualFile selectedFile) {
    return selectedFile != null
        && selectedFile.isDirectory()
        && selectedFile.getName().equalsIgnoreCase("resources");
  }

  private void showErrorMessage(String message, Project project) {
    Messages.showMessageDialog(project, message, "Error", Messages.getInformationIcon());
  }
}
