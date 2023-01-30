package com.github.hervian.rip.util.mappers;

import com.github.hervian.rip.GenerateDocMojo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface GenerateDocMojoMapper extends MojoMapper<GenerateDocMojo> {

  GenerateDocMojoMapper INSTANCE = Mappers.getMapper(GenerateDocMojoMapper.class);

}
