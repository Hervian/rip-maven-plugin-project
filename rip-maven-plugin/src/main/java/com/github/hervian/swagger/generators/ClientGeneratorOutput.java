package com.github.hervian.swagger.generators;

import com.github.hervian.swagger.config.GenerateClientConfig;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientGeneratorOutput {

  private GenerateClientConfig.Language language;
  private String path;

}
