package org.axway.grapes.server.webapp.tasks;

import com.google.common.collect.ImmutableMultimap;
import org.axway.grapes.commons.api.ServerAPI;
import org.axway.grapes.server.db.RepositoryHandler;
import org.axway.grapes.server.db.datamodel.DbCredential.AvailableRoles;
import org.junit.Test;

import java.io.PrintWriter;

import static junit.framework.TestCase.assertNull;
import static org.mockito.Mockito.*;

public class AddRoleTaskTest {

    @Test
    public void testAddRole(){
        final RepositoryHandler repositoryHandler = mock(RepositoryHandler.class);
        final AddRoleTask task = new AddRoleTask(repositoryHandler);

        final ImmutableMultimap.Builder<String, String> builder = new ImmutableMultimap.Builder<String, String>();
        builder.put(ServerAPI.USER_PARAM, "user");
        builder.put(ServerAPI.USER_ROLE_PARAM, "data_updater");
        Exception exception = null;

        try {
            task.execute(builder.build(), mock(PrintWriter.class));
        } catch (Exception e) {
            exception = e;
        }

        assertNull(exception);
        verify(repositoryHandler, times(1)).addUserRole("user", AvailableRoles.DATA_UPDATER);

    }
}

