package org.redpill.alfresco.repo.statistics.service;

import java.util.List;

public class UserProfileStats {

  private List<Integer> summary;

  private String userName;

  private boolean uploadedPicture;

  private String pictureFilename;

  private String updated;

  private String nickName;

  private String jobTitle;

  private String officePhone;

  private String mobilePhone;

  private String location;

  private String company;

  private String about;

  private String localIntranet;

  private String skills;

  public List<Integer> getSummary() {
    return summary;
  }

  public void setSummary(List<Integer> summary) {
    this.summary = summary;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public boolean getUploadedPicture() {
    return uploadedPicture;
  }

  public void setUploadedPicture(boolean uploadedPicture) {
    this.uploadedPicture = uploadedPicture;
  }

  public String getPictureFilename() {
    return pictureFilename;
  }

  public void setPictureFilename(String pictuerFilename) {
    this.pictureFilename = pictuerFilename;
  }

  public String getUpdated() {
    return updated;
  }

  public void setUpdated(String updated) {
    this.updated = updated;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getOfficePhone() {
    return officePhone;
  }

  public void setOfficePhone(String officePhone) {
    this.officePhone = officePhone;
  }

  public String getMobilePhone() {
    return mobilePhone;
  }

  public void setMobilePhone(String mobilePhone) {
    this.mobilePhone = mobilePhone;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public String getLocalIntranet() {
    return localIntranet;
  }

  public void setLocalIntranet(String localIntranet) {
    this.localIntranet = localIntranet;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

}
