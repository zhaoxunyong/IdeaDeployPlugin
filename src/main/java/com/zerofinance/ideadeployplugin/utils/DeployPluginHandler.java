package com.zerofinance.ideadeployplugin.utils;

import com.google.common.collect.Lists;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.zerofinance.ideadeployplugin.exception.DeployPluginException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * <a href="DeployPluginHandler.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
@SuppressWarnings("restriction")
public final class DeployPluginHandler {
    private Project project;
    
    private final static String CHANGEVERSION_BAT = "./changeVersion.sh";
    private final static String RELEASE_BAT = "./release.sh";
    private final static String NEWBRANCH_BAT = "./newBranch.sh";
    private final static String TAG_BAT = "./tag.sh";
    private final static String GITCHECK_BAT = "./gitCheck.sh";
    private final static String COMMITED_LOGS_BAT = "./committedLogs.sh";
    
    @Deprecated
    private final static String MERGE_BAT = "./merge.sh";
    private final static String MYBATISGEN_BAT = "./mybatisGen.sh";
    
    /**
     * 
     */
    public DeployPluginHandler(Project project) {
        this.project = project;
    }
    

    private boolean preCheck(String modulePath) throws Exception {
    	boolean isConfirm = true;
        ExecuteResult result = null;
        try {
            result = this.gitCheck(modulePath);
        } catch (Exception e) {
        	// Skipping check when the gitCheck.sh isn't existing
        	System.err.println(e);
        }
        if(result != null && result.getCode() != 0) {
//        	MessageDialog.openError(shell, "Local Out Of Date", result.getResult());
        	throw new Exception(result.getResult());
        } else {
        	try {
        		result = this.committedLogWarn(modulePath);
        	} catch (Exception e) {
        		System.err.println(e);
        	}
        	if(result != null) {
//            	MessageDialog.openError(shell, "Local Out Of Date", result.getResult());
//            	throw new Exception(result.getResult());
                isConfirm = Messages.showYesNoDialog("Did you forget merging some modified code?\n\n"+result.getResult(), "Committed Log Confirm?", null) == 0;
            }
        }
        return isConfirm;
    }
    
    /*private void clearConsole() {
        MessageConsole cs = DeployPluginHelper.findConsole();
        cs.clearConsole();
        cs.activate();
    }*/

    /**
     * http://www.vogella.com/tutorials/EclipseDialogs/article.html
     */
    private String input(String name, String defaultValue, String example) throws Exception {
        String input = Messages.showInputDialog("Enter parameter, example: " + example, "Enter parameter", null, defaultValue, new InputValidator() {

            @Override
            public boolean checkInput(@NlsSafe String inputString) {
                /*if(StringUtils.isBlank(inputString)) {
                    throw new DeployPluginException("Parameter must not be empty.");
                }*/
                return true;
            }

            @Override
            public boolean canClose(@NlsSafe String inputString) {
                return true;
            }
        });
        if(StringUtils.isBlank(input)) {
            throw new DeployPluginException("Must not be empty.");
        }
        return input;
    }
    
    private String desc(String name) throws Exception {
        String input = Messages.showInputDialog("Add a message for git description", "Description", null, "", new InputValidator() {

            @Override
            public boolean checkInput(@NlsSafe String inputString) {
                return true;
            }

            @Override
            public boolean canClose(@NlsSafe String inputString) {
                return true;
            }
        });
        if(StringUtils.isBlank(input)) {
            throw new DeployPluginException("Must not be empty.");
        }
        return input;
    }
    
    private int compareVersion(String version1, String version2) {
    	if(version1==null || version2==null)
    		return 0;
    	
    	String[] str1 = version1.split("\\.");
    	String[] str2 = version2.split("\\.");
    	
    	for(int i=0; i<str1.length || i<str2.length;){
    		int n1 = i<str1.length ? Integer.parseInt(str1[i]) : 0;
    		int n2 = i<str2.length ? Integer.parseInt(str2[i]) : 0;
    		if(n1 > n2) return 1;
    		else if(n1 < n2) return -1;
    		else i++;			
    	}
    	
    	return 0;
    }
    
    private ExecuteResult gitCheck(String modulePath) throws Exception {
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
    	String cmdFile = FileHandlerUtils.processScript(rootProjectPath, GITCHECK_BAT);
//    	String projectName = project.getLocation().toFile().getName();
//    	String tempProjectFolder = FileHandlerUtils.getTempFolder()+"/"+projectName;
    	List<String> params = Lists.newArrayList();
    	ExecuteResult executeResult = DeployPluginHelper.exec(rootProjectPath, cmdFile, params, true);
        return executeResult;
    }
    
    private ExecuteResult committedLogWarn(String modulePath) throws Exception {
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
    	String cmdFile = FileHandlerUtils.processScript(rootProjectPath, COMMITED_LOGS_BAT);
//    	String projectName = project.getLocation().toFile().getName();
//    	String tempProjectFolder = FileHandlerUtils.getTempFolder()+"/"+projectName;
    	List<String> params = Lists.newArrayList();
    	ExecuteResult executeResult = DeployPluginHelper.exec(rootProjectPath, cmdFile, params, true);
        return executeResult;
    }
    
    private String[] lsRemote(String rootProjectPath) throws Exception {
    	String command = "git";
        List<String> parameters = Lists.newArrayList("ls-remote");
//    	String command = "git ls-remote | grep -v '\\^{}' |  grep 'refs/heads' |awk '{print $NF}' | sed 's;refs/heads/;;g' | sort -t '.' -r -k 2 -V|egrep -i '(release|hotfix)$'";
//      List<String> parameters = Lists.newArrayList();
        ExecuteResult executeResult = DeployPluginHelper.exec(rootProjectPath, command, parameters, false);
        // String result = CmdExecutor.exec(rootProjectPath, command, parameters);
//	        System.out.println("result----->"+result);
        int code = executeResult.getCode();
        String result = executeResult.getResult();
        if(code == 0 && !"".equals(result)) {
        	return result.split("[\n|\r\n]");
        }
        return null;
    }
    
    private String getCurrentDevelopVersion(String[] results) throws Exception {
    	String currentBranch = "";
        if(results != null) {
		    String version2 = "0.0.0";
    		for(String version : results) {
    			if(version.endsWith(".x")) {
//		            String remoteBranchVersion = version.split("/")[2];
		            String remoteBranchVersion = StringUtils.substringAfterLast(version, "/");//.replace(".release", "").replace(".hotfix", "");
		            String version1 = remoteBranchVersion;
		            if(compareVersion(version1, version2) == 1) {
		            	version2 = version1;
		                currentBranch = remoteBranchVersion;
		            }
		        }
		    }
        }
        return currentBranch;
    }
    
    @SuppressWarnings("unchecked")
    private String getRemoteVersion(String[] results) throws Exception {
    	String currentBranch = "";
        if(results != null) {
		    String version2 = "0.0.0";
    		for(String version : results) {
    			if(version.endsWith(".release") || version.endsWith(".hotfix")) {
//		            String remoteBranchVersion = version.split("/")[2];
		            String remoteBranchVersion = StringUtils.substringAfterLast(version, "/");//.replace(".release", "").replace(".hotfix", "");
		            String version1 = remoteBranchVersion.replaceAll("\\.release|\\.hotfix", "");
		            if(compareVersion(version1, version2) == 1) {
		            	version2 = version1;
		                currentBranch = remoteBranchVersion;
		            }
		        }
		    }
        }
        return currentBranch;
    }
    
    @SuppressWarnings("unchecked")
    private String getMavenPomVersion(String rootProjectPath) throws Exception {
        String version = "";
        String pomFile = rootProjectPath+"/pom.xml";
        if(new File(pomFile).exists()) {
            List<String> value = FileUtils.readLines(new File(pomFile));
            for(String v : value) {
                if(v.indexOf("<version>")!=-1) {
                    version = StringUtils.substringBetween(v, "<version>", "</version>");
                    break;
                }
            }
        } else {
//        	throw new DeployPluginException("It seems not a maven project, if you're using non-maven project, please handle it by vscode plugin!");
        	String[] results = this.lsRemote(rootProjectPath);
        	String currentDevelopVersion = getCurrentDevelopVersion(results);
        	if(StringUtils.isBlank(currentDevelopVersion)) {
        		throw new Exception("Please create a branch first.");
        	}
        	String[] currentBranchs = currentDevelopVersion.split("[.]");
        	String b1 = currentBranchs[0];
        	String b2 = currentBranchs[1];
        	version = getRemoteVersion(results).replaceAll("\\.(release|hotfix)$", "");
        	if(StringUtils.isBlank(version)) {
        		// Not found the lastest release version, using the current branch instead
        		version = b1+"."+b2+".0";
        	} else {
            	String[] currentReleases = version.split("[.]");
            	if (currentReleases.length >= 3) {
                    String p1 = currentReleases[0];
                    String p2 = currentReleases[1];
                    String p3 = currentReleases[2];
                    String compareBranch = b1 + b2;
                    String compareTag = p1 + p2;
                    if (!compareBranch.equals(compareTag)) {
                    	version = b1 + "." + b2 + ".0";
                    } else {
                        int nextP3 = Integer.parseInt(p3) + 1;
                        version = p1 + '.' + p2 + '.' + nextP3;
                    }
                }
        		
        	}
        }
        return version;
    }
    
    private String haSnapshotVersion(List<String> value, String var) {
        for(String v : value) {
            if(v.indexOf(var) !=-1 && v.indexOf("-SNAPSHOT") != -1) {
                return v.trim();
            }
        }
        return "";
    }
    
    @SuppressWarnings("unchecked")
    private String checkHasSnapshotVersion(String rootProjectPath) throws IOException {
        File dir = new File(rootProjectPath);  
        Collection<File> files = FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter("pom.xml"), DirectoryFileFilter.DIRECTORY);  
         for (File f : files) {    
             String pomFile = f.getPath();
             List<String> value = FileUtils.readLines(new File(pomFile));
             boolean isNew = false;
             for(String v : value) {
                 if(v.indexOf("<dependency>") !=-1) {
                     isNew = true;
                 } else if(v.indexOf("</dependency>") !=-1) {
                     isNew = false;
                 }
                 
                 if(isNew && v.indexOf("version")!=-1) {
                     // && v.indexOf("-SNAPSHOT") != -1
                     if(v.indexOf("${") != -1) {
                         String var = StringUtils.substringBetween(v, "${", "}");
                         String snapshotVar = haSnapshotVersion(value, var);
                         if(StringUtils.isNotBlank(snapshotVar)) {
                             return pomFile+"("+snapshotVar+")";
                         }
                     } else if(v.indexOf("-SNAPSHOT") != -1) {
                         return pomFile+"("+v.trim()+")";
                     }
                 }
             }    
         }
         return "";
    }

    public void changeVersion(String modulePath, String name) throws Exception {
        String cmdFile = FileHandlerUtils.processScript(modulePath, CHANGEVERSION_BAT);
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
//        String cmdName = FilenameUtils.getName(cmdFile);
        String pomVersion = getMavenPomVersion(rootProjectPath);
        String bPomVersion = StringUtils.substringBeforeLast(pomVersion, ".");
        String aPomVersion = StringUtils.substringAfterLast(pomVersion, ".").replace("-SNAPSHOT", "");
        aPomVersion = String.valueOf(Integer.parseInt(aPomVersion)+1);
        String defaultValue = bPomVersion+"."+aPomVersion+"-SNAPSHOT";
        List<String> parameters = Lists.newArrayList();
        String params = input(name, defaultValue, "newVersion");
        if(StringUtils.isNotBlank(params)) {
            parameters.add(params);
            
            if(parameters!=null && !parameters.isEmpty()) {
                CmdBuilder cmdBuilder = new CmdBuilder(rootProjectPath, cmdFile, true, parameters);
                runJob(cmdBuilder);
            }
        }
    }

    public void newBranch(String modulePath, String name) throws Exception {
        String cmdFile = FileHandlerUtils.processScript(modulePath, NEWBRANCH_BAT);
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
//        String cmdName = FilenameUtils.getName(cmdFile);
        String pomVersion = getMavenPomVersion(rootProjectPath);
        // 1.5.6->1.6.x
        String bPomVersion = StringUtils.substringBeforeLast(pomVersion, "."); //1.5
        String a1PomVersion = StringUtils.substringBeforeLast(bPomVersion, "."); // 1
        String a2PomVersion = StringUtils.substringAfterLast(bPomVersion, "."); // 5
        a2PomVersion = String.valueOf(Integer.parseInt(a2PomVersion)+1);
        String defaultValue = a1PomVersion+"."+a2PomVersion+".x"; // 1.6.x
        List<String> parameters = Lists.newArrayList();
        String params = input(name, defaultValue, "newBranch");
        parameters.add(params);
        
        if(parameters!=null && !parameters.isEmpty()) {
//            String projectPath = project.getLocation().toFile().getPath();
//            String rootProjectPath = getParentProject(projectPath, cmd);
            String desc = desc(name);
            if(StringUtils.isNotBlank(desc)) {
//            	params = params + " '" + desc +"'";
            	parameters.add(desc);
                CmdBuilder cmdBuilder = new CmdBuilder(rootProjectPath, cmdFile, true, parameters);
                runJob(cmdBuilder);

            }
        }
    }

    public void mybatisGen(String modulePath, String name) throws Exception {
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);

        String cmdFile = FileHandlerUtils.processScript(modulePath, MYBATISGEN_BAT);
//        String cmdName = FilenameUtils.getName(cmdFile);
        // Using "projectPath" instead of "rootProjectPath"
        CmdBuilder cmdBuilder = new CmdBuilder(rootProjectPath, cmdFile, true, Lists.newArrayList());
        boolean isConfirm = Messages.showYesNoDialog(name + " Mybatis Gen Confirm?", "Mybatis Gen Confirm?", null) == 0;
        // boolean isConfirm = MessageDialog.openConfirm(shell, "Mybatis Gen Confirm?", project.getName() + " Mybatis Gen Confirm?");
        if(isConfirm) {
            runJob(cmdBuilder);
        }
    }
    
	private List<String> getReleaseList(String modulePath) throws Exception {
    	List<String> allReleases = Lists.newArrayList();
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
    	
        String command = "git";
        List<String> parameters = Lists.newArrayList("ls-remote");
        ExecuteResult executeResult = DeployPluginHelper.exec(rootProjectPath, command, parameters, false);
        int code = executeResult.getCode();
        String result = executeResult.getResult();
        if(code == 0 && !"".equals(result)) {
            String[] results = result.split("[\n|\r\n]");
            for(String r : results) {
                if(r.endsWith(".release") || r.endsWith(".hotfix")) {
                    String version = StringUtils.substringAfterLast(r, "/");//.replace(".release", "").replace(".hotfix", "");
                    allReleases.add(version);
                }
            }
        }

    	return allReleases;
	}

    public void release(String modulePath, String name) throws Exception {
        String rootProjectPath = FileHandlerUtils.getRootProjectPath(modulePath);
        
        boolean continute = true;
        try {
	        String snapshotPath = checkHasSnapshotVersion(rootProjectPath);
	        if(StringUtils.isNotBlank(snapshotPath)) {
                continute = Messages.showYesNoDialog("There is a SNAPSHOT version in "+snapshotPath+", when the version is released, it's suggested to replace it as release version. Do you want to continue?",
                        "process confirm?",
                        null) == 0;
	            // continute = MessageDialog.openConfirm(shell, "process confirm?", "There is a SNAPSHOT version in "+snapshotPath+", when the version is released, it's suggested to replace it as release version. Do you want to continue?");
	        }
        }catch(Exception e) {
        	// do nothing
        }
        
        if(continute) {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        	String dateString = formatter.format(new Date());

            String releaseType = Messages.showEditableChooseDialog("Which release type do you want to pick?", "Choose", null, new String[]{"release", "hotfix"}, "release",null);
            if(!"release".equals(releaseType) && !"hotfix".equals(releaseType)) {
                throw new DeployPluginException("Please pick up either release or hotfix option.");
            }
            String cmdFile = FileHandlerUtils.processScript(rootProjectPath, RELEASE_BAT);
//		            String cmdName = FilenameUtils.getName(cmdFile);

            String pomVersion = getMavenPomVersion(rootProjectPath);
            String defaultValue = pomVersion.replace("-SNAPSHOT", "")+"."+releaseType;

            String inputedVersion = input(name, defaultValue, "BranchVersion TagDate").trim();
            if( inputedVersion.indexOf(" ") != -1) {
                throw new Exception("The version is invalid.");
            }

            if(StringUtils.isNotBlank(inputedVersion)) {
//		                String projectPath = project.getLocation().toFile().getPath();
//		                String rootProjectPath = getParentProject(projectPath, cmd);

                String desc = desc(name);
                if(StringUtils.isNotBlank(desc)) {
                    List<String> parameters = Lists.newArrayList(inputedVersion, dateString, "false", "\""+desc+"\"");
                    CmdBuilder cmdBuilder = new CmdBuilder(rootProjectPath, cmdFile, true, parameters);
                    runJob(cmdBuilder);
                }
            }
        }
    }

    private void runJob(CmdBuilder cmdBuilder) {
        try {
            // https://stackoverflow.com/questions/51972122/intellij-plugin-development-print-in-console-window
            // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206756385-How-to-make-a-simple-console-output
            String rootProjectPath = cmdBuilder.getWorkHome();
            String command = cmdBuilder.getCommand();
            List<String> params = cmdBuilder.getParams();
            String title = new File(rootProjectPath).getName();
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(DeployPluginHelper.PLUGIN_ID);
            toolWindow.show();
            toolWindow.activate(null);

            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            //Content content = toolWindow.getContentManager().findContent(title);
            //if(content == null) {
            Content content = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), title, false);
            toolWindow.getContentManager().addContent(content);
            //}
            //consoleView.clear();
            //consoleView.print("Hello from MyPlugin!"+new Date().toString(), ConsoleViewContentType.NORMAL_OUTPUT);
            //command = "./gradlew build";
            ExecuteResult result = DeployPluginHelper.exec(consoleView, rootProjectPath, command, params, true);
            System.out.println("code="+result.getCode());
            System.out.println("result="+result.getResult());

            /*TerminalView terminalView = TerminalView.getInstance(project);
            ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
            if (window == null) {
                return;
            }
            window.activate(null);
            ContentManager contentManager = window.getContentManager();
            Content content = contentManager.findContent(title);

            if(content == null) {
                ShellTerminalWidget terminal = terminalView.createLocalShellWidget(project.getBasePath(), title);
                terminal.getTerminalTextBuffer().addModelListener(() -> {
                    String text = terminal.getTerminalTextBuffer().getLine(0).getText();
                    System.out.println("text------>"+text);
                });
                terminal.executeCommand(command);
            } else {
                Pair<Content, ShellTerminalWidget> pair = getSuitableProcess(content);
                if(pair == null) {
                    //Messages.showInfoMessage("A terminal has been running", "Warnning");
                    showMessage("A terminal has been running", title, NotificationType.ERROR);
                } else {
                    pair.first.setDisplayName(title);
                    contentManager.setSelectedContent(pair.first);
                    ShellTerminalWidget terminal = pair.second;
                    terminal.executeCommand(command);
                }

            }*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
