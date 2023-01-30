package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.GenerateUiMojo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenerateUiMojoMapper extends MojoMapper<GenerateUiMojo> {

  GenerateUiMojoMapper INSTANCE = Mappers.getMapper(GenerateUiMojoMapper.class);

}
