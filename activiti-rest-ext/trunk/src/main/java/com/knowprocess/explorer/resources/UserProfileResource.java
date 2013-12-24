package com.knowprocess.explorer.resources;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import com.knowprocess.activiti.model.GroupVO;
import com.knowprocess.activiti.model.ProfileVO;

/**
 * User profile resource allowing creation and retrieval of User, Group and
 * UserInfo data in a single call.
 * 
 * @author timstephenson
 * 
 */
@Path("/ext/profile")
public class UserProfileResource {

    @Context
    private UriInfo uriInfo;
    private ProcessEngine processEngine;
    private Logger logger = Logger.getLogger(getClass().getName());

    public UserProfileResource() {
        processEngine = ProcessEngines.getDefaultProcessEngine();
        assert processEngine != null;
    }

    @GET
    @Path("/{username}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public Response getProfile(@PathParam("username") String username) {
        IdentityService svc = processEngine.getIdentityService();
        User user = svc.createUserQuery().userId(username).singleResult();
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            ProfileVO profile = new ProfileVO().setUser(user);

            List<Group> list = svc.createGroupQuery().groupMember(username)
                    .list();
            for (Group group : list) {
                profile.addGroup(group);
            }

            List<String> userInfoKeys = svc.getUserInfoKeys(username);
            for (String key : userInfoKeys) {
                profile.addInfo(key, svc.getUserInfo(username, key));
            }

            return Response.ok(profile).build();
        }
    }

	@PUT
    @Path("/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateProfile(@Context SecurityContext sc, ProfileVO profile) {
		System.out.println("Attempt to update: " + profile.getUsername());
        IdentityService idSvc = processEngine.getIdentityService();
		User user = idSvc.createUserQuery().userId(profile.getUsername())
				.singleResult();
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
			if (profile.getFirstName() != null
					&& profile.getFirstName().trim().length() > 0) {
				user.setFirstName(profile.getFirstName());
            }
			if (profile.getLastName() != null
					&& profile.getLastName().trim().length() > 0) {
				user.setLastName(profile.getLastName());
            }
			if (profile.getEmail() != null
					&& profile.getEmail().trim().length() > 0) {
				user.setEmail(profile.getEmail());
            }
            idSvc.saveUser(user);
			for (GroupVO group : profile.getGroups()) {
				System.out.println("Adding membership of " + group.getId()
						+ " to " + profile.getId());
				try {
					idSvc.createMembership(profile.getId(), group.getId());
				} catch (Exception e) {
					// TODO narrow exception
					System.out
							.println("WARNING: Check this is because attempting to create membership a second time");
					System.out.println(e.getClass().getName() + ":"
							+ e.getMessage());
				}
			}
			for (Entry<String, String> info : profile.getInfo().entrySet()) {
				idSvc.setUserInfo(profile.getId(), info.getKey(),
						info.getValue());
			}
            return Response.ok().build();
        }
    }
}