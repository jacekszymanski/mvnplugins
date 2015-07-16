/**
 * Copyright (C) 2009 Progress Software, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.mvnplugins.provision;

import java.io.File;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * A Maven Mojo to install a provision in the local repo if its not already there and then
 * provision an artifact into an output directory
 *
 * @author <a href="http://macstrac.blogspot.com">James Strachan</a>
 */
@Mojo(defaultPhase = LifecyclePhase.INSTALL, name="provision")
@Execute(phase=LifecyclePhase.INSTALL)
public class ProvisionMojo extends AbstractMojo {

    /**
     * The directory where the output files will be located.
     */
	@Parameter(defaultValue = "${outputDirectory}", required = true)
    protected File outputDirectory;


    /**
     * The maven project.
     *
     */
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

	
	@Parameter(defaultValue = "${project.build.finalName}", required = true)
	protected String finalName;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if ("pom".equals(project.getPackaging())) {
            getLog().debug("Ignoring pom packaging");
            return;
        }
        Build build = project.getBuild();
        if (build == null) {
            getLog().debug("No Build available in this Project");
            return;
        }
        
        
        
        File file = new File(build.getDirectory(), finalName + "." + getArtifactExtension());
        getLog().debug("Trying to detect: " + file.getAbsolutePath());

        if (file.exists()) {
            File destFile = new File(outputDirectory, file.getName());
            try {
                getLog().info("Copying "
                        + file.getName() + " to "
                        + destFile);
                FileUtils.copyFile(file, destFile);

            } catch (Exception e) {
                throw new MojoExecutionException("Error copying artifact from " + file + " to " + destFile, e);
            }
        } else {
            getLog().info("Artifact does not exist so cannot be provisioned: " + file.getPath());
        }
    }

    private String getArtifactExtension() {
        String packaging = project.getPackaging();
        if ("bundle".equals(packaging)) {
            return "jar";
        } else {
            return packaging;
        }
    }
}
