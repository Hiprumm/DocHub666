package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新用户请求 DTO（密码为可选字段：传入则重设并 BCrypt 加密，未传则保持原密码）。
 */
@Getter
@Setter
public class SysUserUpdateDto {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 可选：修改密码时传入原明文，由 Service 进行 BCrypt 加密后覆盖 */
    @Size(min = 8, max = 64, message = "密码长度需在 8~64 之间")
    private String password;

    /** 乐观锁版本号（必须携带客户端读取到的当前版本） */
    @NotNull(message = "必须携带当前版本号")
    private Integer version;

    @Size(max = 50, message = "真实姓名最长 50 个字符")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 个字符")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Long deptId;

    private Integer status;
}