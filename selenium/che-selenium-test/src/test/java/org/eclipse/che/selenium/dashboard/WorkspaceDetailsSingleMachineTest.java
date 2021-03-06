/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.RUNNING;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.INSTALLERS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.PROJECTS;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames.SERVERS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceInstallers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceMachines;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceProjects;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceServers;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class WorkspaceDetailsSingleMachineTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final ImmutableMap<String, Boolean> EXPECTED_INSTALLERS =
      ImmutableMap.<String, Boolean>builder()
          .put("C# language server", false)
          .put("Exec", false)
          .put("File sync", false)
          .put("Git credentials", false)
          .put("JSON language server", false)
          .put("PHP language server", false)
          .put("Python language server", false)
          .put("Simple Test language server", false)
          .put("SSH", false)
          .put("Terminal", true)
          .put("TypeScript language server", false)
          .put("Yaml language server", false)
          .build();

  private String workspaceName;

  @Inject private TestUser testUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Dashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private WorkspaceProjects workspaceProjects;
  @Inject private Workspaces workspaces;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private WorkspaceMachines workspaceMachines;
  @Inject private WorkspaceServers workspaceServers;
  @Inject private WorkspaceInstallers workspaceInstallers;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private MachineTerminal terminal;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    workspaceName = testWorkspace.getName();
    URL resource =
        WorkspaceDetailsSingleMachineTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);

    dashboard.open();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaces.waitToolbarTitleName(workspaceName);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceDetails.checkStateOfWorkspace(RUNNING);
    workspaceDetails.clickOnStopWorkspace();
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(workspaceName, testUser.getName());
  }

  @Test
  public void checkOverviewTab() {
    workspaceOverview.checkNameWorkspace(workspaceName);
    workspaceOverview.isDeleteWorkspaceButtonExists();

    // check the Export feature
    workspaceOverview.clickExportWorkspaceBtn();
    workspaceOverview.waitClipboardWorkspaceJsonFileBtn();
    workspaceOverview.waitDownloadWorkspaceJsonFileBtn();
    workspaceOverview.clickOnHideWorkspaceJsonFileBtn();
  }

  @Test
  public void checkWorkingWithInstallers() {
    workspaceDetails.selectTabInWorkspaceMenu(INSTALLERS);

    // check both versions of the 'Workspace API' installer
    assertTrue(workspaceInstallers.isInstallerStateTurnedOn("Workspace API", "1.0.1"));
    assertFalse(workspaceInstallers.isInstallerStateTurnedOn("Workspace API", "1.0.0"));
    assertTrue(workspaceInstallers.isInstallerStateNotChangeable("Workspace API", "1.0.1"));
    assertTrue(workspaceInstallers.isInstallerStateNotChangeable("Workspace API", "1.0.0"));

    // check all needed installers in dev-machine exist
    workspaceMachines.selectMachine("Workspace Installers", "dev-machine");
    EXPECTED_INSTALLERS.forEach(
        (name, value) -> {
          workspaceInstallers.checkInstallerExists(name);
        });

    // switch all installers and save changes
    EXPECTED_INSTALLERS.forEach(
        (name, value) -> {
          assertEquals(workspaceInstallers.isInstallerStateTurnedOn(name), value);
          workspaceInstallers.switchInstallerState(name);
          WaitUtils.sleepQuietly(1);
        });
    clickOnSaveButton();

    // switch all installers, save changes and check its states are as previous(by default for the
    // Java stack)
    EXPECTED_INSTALLERS.forEach(
        (name, value) -> {
          workspaceInstallers.switchInstallerState(name);
          loader.waitOnClosed();
        });
    clickOnSaveButton();
    EXPECTED_INSTALLERS.forEach(
        (name, value) -> {
          assertEquals(workspaceInstallers.isInstallerStateTurnedOn(name), value);
        });
  }

  @Test
  public void checkWorkingWithServers() {
    workspaceDetails.selectTabInWorkspaceMenu(SERVERS);

    // add a new server, save changes and check it exists
    createServer("agen", "8083", "https");

    // edit the server and check it exists
    workspaceServers.clickOnEditServerButton("agen");
    workspaceServers.enterReference("agent");
    workspaceServers.enterPort("83");
    workspaceServers.enterProtocol("http");
    workspaceDetails.clickOnUpdateButtonInDialogWindow();
    workspaceServers.checkServerExists("agent", "83");

    // delete the server and check it is not exist
    workspaceServers.clickOnDeleteServerButton("agent");
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerIsNotExists("agent", "83");

    // add a new server which will be checked after the workspace staring
    createServer("agent", "8082", "https");
  }

  @Test
  public void checkWorkingWithProjects() {
    workspaceDetails.selectTabInWorkspaceMenu(PROJECTS);

    // add new project and cancel adding
    addNewProject(WEB_JAVA_SPRING);
    workspaceDetails.clickOnCancelChangesBtn();
    workspaceProjects.waitProjectIsNotPresent(WEB_JAVA_SPRING);

    // add two projects and save changes
    workspaceProjects.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.selectSample(CONSOLE_JAVA_SIMPLE);
    projectSourcePage.clickOnAddProjectButton();
    clickOnSaveButton();

    // check that project exists(workspace will restart)
    workspaceProjects.waitProjectIsPresent(WEB_JAVA_SPRING);
    workspaceProjects.waitProjectIsPresent(CONSOLE_JAVA_SIMPLE);
  }

  @Test(priority = 1)
  public void startWorkspaceAndCheckChanges() {
    // start the workspace and check that the added projects exist
    workspaceDetails.clickOpenInIdeWsBtn();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab(LOADER_TIMEOUT_SEC);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME, PROJECT_FOLDER);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(CONSOLE_JAVA_SIMPLE, PROJECT_FOLDER);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(WEB_JAVA_SPRING, PROJECT_FOLDER);

    // check that the added "agent" server is existed in the Servers tab
    checkServerInServersList("agent");
  }

  private void addNewProject(String projectName) {
    workspaceProjects.clickOnAddNewProjectButton();
    projectSourcePage.selectSample(projectName);
    projectSourcePage.clickOnAddProjectButton();
    workspaceProjects.waitProjectIsPresent(projectName);
  }

  private void clickOnSaveButton() {
    workspaceDetails.clickOnSaveChangesBtn();
    dashboard.waitNotificationMessage("Workspace updated");
    dashboard.waitNotificationIsClosed();
  }

  private void createServer(String serverName, String serverPort, String serverProtocol) {
    workspaceServers.clickOnAddServerButton();
    workspaceServers.waitAddServerDialogIsOpen();
    workspaceServers.enterReference(serverName);
    workspaceServers.enterPort(serverPort);
    workspaceServers.enterProtocol(serverProtocol);
    workspaceDetails.clickOnAddButtonInDialogWindow();
    clickOnSaveButton();
    workspaceServers.checkServerExists(serverName, serverPort);
  }

  private void checkServerInServersList(String serverName) {
    consoles.openServersTabFromContextMenu("dev-machine");
    consoles.waitProcessInProcessConsoleTree("Servers");
    consoles.waitTabNameProcessIsPresent("Servers");
    consoles.waitExpectedTextIntoServerTableCation("Servers of dev-machine:");
    assertTrue(consoles.checkThatServerExists(serverName));
  }
}
