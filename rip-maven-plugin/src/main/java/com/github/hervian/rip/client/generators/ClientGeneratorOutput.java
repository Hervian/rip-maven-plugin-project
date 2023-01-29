package com.github.hervian.rip.client.generators;

import com.github.hervian.rip.config.GenerateClientConfig;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientGeneratorOutput {

  private GenerateClientConfig.Language language;
  private String path;

}
