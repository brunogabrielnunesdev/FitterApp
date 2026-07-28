package com.fitterapp.auth.mapper;

import com.fitterapp.auth.dto.login.LoginRequestDto;
import com.fitterapp.auth.dto.login.LoginResponseDto;
import com.fitterapp.auth.dto.register.RegisterRequestDto;
import com.fitterapp.auth.dto.register.RegisterResponseDto;
import com.fitterapp.auth.service.login.LoginCommand;
import com.fitterapp.auth.service.login.LoginResult;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.register.RegisterResult;
import com.fitterapp.common.config.CentralMapperConfig;
import java.net.InetAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface AuthMapper {

  RegisterCommand toCommand(RegisterRequestDto request);

  RegisterResponseDto toResponse(RegisterResult result);

  LoginCommand toCommand(LoginRequestDto request, String userAgent, InetAddress ipAddress);

  @Mapping(target = "tokenType", constant = "Bearer")
  LoginResponseDto toResponse(LoginResult result);
}
