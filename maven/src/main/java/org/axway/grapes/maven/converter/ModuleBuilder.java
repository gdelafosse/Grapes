package org.axway.grapes.maven.converter;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.License;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.axway.grapes.commons.datamodel.Artifact;
import org.axway.grapes.commons.datamodel.Dependency;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.maven.resolver.ArtifactResolver;
import org.axway.grapes.maven.resolver.LicenseResolver;

import java.util.List;

/**
 * Data GrapesTranslator Implementation
 *
 * <p>Ensures the transformation from Maven data model to Grapes data model.</p>
 *
 * @author jdcoffre
 */
public class ModuleBuilder {

    private final LicenseResolver licenseResolver;
    private final ArtifactResolver artifactResolver;

    private final Log log;

    public ModuleBuilder(final RepositorySystem repositorySystem, final ArtifactRepository localRepository, final MavenProject project, final Log log) {
        this.licenseResolver = new LicenseResolver(repositorySystem, localRepository, project, log);
        this.artifactResolver = new ArtifactResolver(repositorySystem, localRepository, project, log);
        this.log = log;
    }

    /**
     * Turn a maven project (Maven data model) into a module (Grapes data model)
     *
     * @param project
     * @return
     */
    public Module build(final MavenProject project) throws MojoExecutionException {
        final Module module = GrapesTranslator.getGrapesModule(project);
        final List<License> licenses = licenseResolver.resolve(project);

        /* Manage Artifacts */
        final Artifact mainArtifact = GrapesTranslator.getGrapesArtifact(project.getArtifact());
        addLicenses(mainArtifact, licenses);
        module.addArtifact(mainArtifact);

        // Trick to add project pom file as a module artifact
        final Artifact pomArtifact = GrapesTranslator.getGrapesArtifact(project.getArtifact());
        addLicenses(pomArtifact, licenses);
        pomArtifact.setType("pom");
        pomArtifact.setExtension("xml");
        module.addArtifact(pomArtifact);
        // End of trick

        for(int i = 0 ; i < project.getAttachedArtifacts().size() ; i++){
            final Artifact attachedArtifact = GrapesTranslator.getGrapesArtifact(project.getAttachedArtifacts().get(i));
            // handle licenses
            addLicenses(attachedArtifact, licenses);
            module.addArtifact(attachedArtifact);
        }

        /* Manage Dependencies */
        for(int i = 0 ; i < project.getDependencies().size() ; i++){
            final Dependency dependency = GrapesTranslator.getGrapesDependency(
                    artifactResolver.resolveArtifact(project.getDependencies().get(i)),
                        project.getDependencies().get(i).getScope());

            // handle licenses
            for(License license: licenseResolver.resolve(
                    dependency.getTarget().getGroupId(),
                    dependency.getTarget().getArtifactId(),
                    dependency.getTarget().getVersion())){
                dependency.getTarget().addLicense(license.getName());
            }

            module.addDependency(dependency);
        }

        return module;
    }

    /**
     * Fill the artifact with the licenses name of the license list
     * @param mainArtifact
     * @param licenses
     */
    private void addLicenses(final Artifact mainArtifact, final List<License> licenses) {
        for(License license: licenses){
            mainArtifact.addLicense(license.getName());
        }
    }

}