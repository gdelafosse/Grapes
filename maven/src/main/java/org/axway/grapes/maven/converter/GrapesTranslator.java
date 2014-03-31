package org.axway.grapes.maven.converter;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.axway.grapes.commons.datamodel.Artifact;
import org.axway.grapes.commons.datamodel.DataModelFactory;
import org.axway.grapes.commons.datamodel.Module;
import org.axway.grapes.commons.exceptions.UnsupportedScopeException;
import org.axway.grapes.commons.utils.FileUtils;
import org.axway.grapes.maven.resolver.ArtifactResolver;

/**
 * Grapes Translator
 *
 * <p>Handles transformation from Maven data model to Grapes data model</p>
 *
 * @author jdcoffre
 */
public class GrapesTranslator {

    /**
     * Generate a Grapes module from a Maven project
     *
     * @param project MavenProject
     * @return Module
     */
    public final static Module getGrapesModule(final MavenProject project) {
        final String moduleName = generateModuleName(project);
        return DataModelFactory.createModule(moduleName, project.getVersion());
    }

    /**
     * Generate module's name from maven project
     *
     * @param project MavenProject
     * @return String
     */
    public static String generateModuleName(final MavenProject project) {
        final StringBuilder sb = new StringBuilder();
        sb.append(project.getArtifact().getGroupId());
        sb.append(":");
        sb.append(project.getArtifact().getArtifactId());
        return sb.toString();
    }

    /**
     * Generate a Grapes artifact from a Maven artifact
     *
     * @param mavenArtifact org.apache.maven.artifact.Artifact
     * @return Artifact
     */
    public final static Artifact getGrapesArtifact(org.apache.maven.artifact.Artifact mavenArtifact) {
        final ArtifactHandler artifactHandler = mavenArtifact.getArtifactHandler();
        String extension = mavenArtifact.getType();

        if(artifactHandler != null){
            extension = artifactHandler.getExtension();
        }

        String version = mavenArtifact.getVersion();

        // Manage version ranges
        if(version == null && mavenArtifact.getVersionRange() != null){
            version = ArtifactResolver.getArtifactVersion(mavenArtifact.getVersionRange());
        }

        final Artifact artifact =  DataModelFactory.createArtifact(
                mavenArtifact.getGroupId(),
                mavenArtifact.getArtifactId(),
                version,
                mavenArtifact.getClassifier(),
                mavenArtifact.getType(),
                extension);

        artifact.setDownloadUrl(mavenArtifact.getDownloadUrl());

        final Long artifactSize = FileUtils.getSize(mavenArtifact.getFile());
        if(artifactSize != null){
            artifact.setSize(String.valueOf(artifactSize));
        }

        return artifact;
    }

    /**
     * Generate a Grapes dependency from a Maven dependency
     *
     * @param dependency Dependency
     * @return org.axway.grapes.commons.datamodel.Dependency
     * @throws MojoExecutionException
     */
    public static org.axway.grapes.commons.datamodel.Dependency getGrapesDependency(final org.apache.maven.artifact.Artifact dependency, String scope) throws MojoExecutionException {
        try {

            final Artifact target = getGrapesArtifact(dependency);
            return DataModelFactory.createDependency(target, scope);

        } catch (UnsupportedScopeException e) {
            throw new MojoExecutionException("Failed to create the dependency" + dependency.toString() , e);
        }
    }
}
