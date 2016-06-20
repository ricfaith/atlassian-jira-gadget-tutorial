package com.atlassian.plugins.tutorial;

import java.util.Collection;
import java.util.LinkedList;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * REST resource that provides a list of projects in JSON format.
 */
@Path("/projects")
public class ProjectsResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    /**
     * Constructor.
     * @param authenticationContext the context from which to retrieve the user
     * @param permissionManager the JIRA object which manages permissions
     * for users and projects
     */
    public ProjectsResource(
            @ComponentImport final JiraAuthenticationContext authenticationContext,
            @ComponentImport final PermissionManager permissionManager) {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns the list of projects browsable by the user in the specified
     * request.
     * @param request the context-injected {@code HttpServletRequest}
     * @return a {@code Response} with the marshalled projects
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getProjects(@Context HttpServletRequest request)
    {
        // get the corresponding com.opensymphony.os.User object for
        // the request
        final ApplicationUser user = authenticationContext.getUser();
 
        // retrieve all objects for projects this user has permission to browse
        Collection<Project> projects =
                permissionManager.getProjects(ProjectPermissions.BROWSE_PROJECTS, user);

        // convert the project objects to ProjectRepresentations
        Collection<ProjectRepresentation> projectRepresentations =
                new LinkedList<ProjectRepresentation>();
        for (Project project : projects)
        {
            projectRepresentations.add(new ProjectRepresentation(project));
        }
        ProjectsRepresentation allProjects =
                new ProjectsRepresentation(projectRepresentations);

        // return the project representations. JAXB will handle the conversion
        // to XML or JSON.
        return Response.ok(allProjects).build();
    }
}
