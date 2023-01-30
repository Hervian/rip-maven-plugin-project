package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.RestInPeaceMojo;
import org.apache.maven.plugin.MojoExecutionException;

public interface MojoMapper<T> {

  T map(RestInPeaceMojo restInPeaceMojo) throws MojoExecutionException;

}
