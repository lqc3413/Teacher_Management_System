package com.teacher.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("users")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String employeeNo;
    private Integer gender;
    private String phone;
    private String email;
    private String avatar;
    private Long roleId;
    private Long deptId;
    private String title;
    private String education;
    private String degree;
    private String major;
    private String school;
    private String degreeDate;
    private Integer isDualTeacher;
    private String skillCert;
    private String researchArea;
    private LocalDate entryDate;
    private Integer status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }

    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }

    public String getDegreeDate() { return degreeDate; }
    public void setDegreeDate(String degreeDate) { this.degreeDate = degreeDate; }

    public Integer getIsDualTeacher() { return isDualTeacher; }
    public void setIsDualTeacher(Integer isDualTeacher) { this.isDualTeacher = isDualTeacher; }

    public String getSkillCert() { return skillCert; }
    public void setSkillCert(String skillCert) { this.skillCert = skillCert; }

    public String getResearchArea() { return researchArea; }
    public void setResearchArea(String researchArea) { this.researchArea = researchArea; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
