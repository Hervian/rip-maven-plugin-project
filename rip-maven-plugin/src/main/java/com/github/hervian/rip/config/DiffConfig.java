package com.github.hervian.rip.config;

import lombok.Data;
import org.apache.maven.plugins.annotations.Parameter;

@Data
public class DiffConfig {

  @Parameter(defaultValue = "false")
  private boolean skipCheckForBreakingChanges;

}
