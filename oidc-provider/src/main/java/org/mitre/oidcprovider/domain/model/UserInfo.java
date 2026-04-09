package org.mitre.oidcprovider.domain.model;

import org.mitre.oidcprovider.domain.model.vo.Address;
import org.mitre.oidcprovider.domain.model.vo.Subject;

import java.util.Set;

/**
 * Aggregate Root representing OIDC UserInfo claims.
 * No framework annotations — pure domain object.
 */
public class UserInfo {

    private final Subject sub;
    private String preferredUsername;
    private String name;
    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;
    private String email;
    private Boolean emailVerified;
    private String phoneNumber;
    private Boolean phoneNumberVerified;
    private String gender;
    private String birthdate;
    private String zoneinfo;
    private String locale;
    private String updatedTime;
    private String profile;
    private String picture;
    private String website;
    private Address address;

    private UserInfo(Subject sub) {
        this.sub = sub;
    }

    /** Factory method — creates a minimal UserInfo with only the required sub claim. */
    public static UserInfo create(Subject sub) {
        if (sub == null) {
            throw new IllegalArgumentException("Subject must not be null");
        }
        return new UserInfo(sub);
    }

    /**
     * Returns a new UserInfo containing only the claims permitted by the given scopes.
     * <ul>
     *   <li>openid  → sub</li>
     *   <li>profile → name, given_name, family_name, middle_name, nickname, preferred_username,
     *                  profile, picture, website, gender, birthdate, zoneinfo, locale, updated_at</li>
     *   <li>email   → email, email_verified</li>
     *   <li>phone   → phone_number, phone_number_verified</li>
     *   <li>address → address</li>
     * </ul>
     */
    public UserInfo filterByClaims(Set<String> scopes) {
        UserInfo filtered = new UserInfo(this.sub);

        if (scopes == null) {
            return filtered;
        }

        if (scopes.contains("profile")) {
            filtered.name = this.name;
            filtered.givenName = this.givenName;
            filtered.familyName = this.familyName;
            filtered.middleName = this.middleName;
            filtered.nickname = this.nickname;
            filtered.preferredUsername = this.preferredUsername;
            filtered.profile = this.profile;
            filtered.picture = this.picture;
            filtered.website = this.website;
            filtered.gender = this.gender;
            filtered.birthdate = this.birthdate;
            filtered.zoneinfo = this.zoneinfo;
            filtered.locale = this.locale;
            filtered.updatedTime = this.updatedTime;
        }

        if (scopes.contains("email")) {
            filtered.email = this.email;
            filtered.emailVerified = this.emailVerified;
        }

        if (scopes.contains("phone")) {
            filtered.phoneNumber = this.phoneNumber;
            filtered.phoneNumberVerified = this.phoneNumberVerified;
        }

        if (scopes.contains("address")) {
            filtered.address = this.address;
        }

        return filtered;
    }

    // Getters
    public Subject getSub() { return sub; }
    public String getPreferredUsername() { return preferredUsername; }
    public String getName() { return name; }
    public String getGivenName() { return givenName; }
    public String getFamilyName() { return familyName; }
    public String getMiddleName() { return middleName; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public Boolean getEmailVerified() { return emailVerified; }
    public String getPhoneNumber() { return phoneNumber; }
    public Boolean getPhoneNumberVerified() { return phoneNumberVerified; }
    public String getGender() { return gender; }
    public String getBirthdate() { return birthdate; }
    public String getZoneinfo() { return zoneinfo; }
    public String getLocale() { return locale; }
    public String getUpdatedTime() { return updatedTime; }
    public String getProfile() { return profile; }
    public String getPicture() { return picture; }
    public String getWebsite() { return website; }
    public Address getAddress() { return address; }

    // Setters (for building/updating)
    public void setPreferredUsername(String preferredUsername) { this.preferredUsername = preferredUsername; }
    public void setName(String name) { this.name = name; }
    public void setGivenName(String givenName) { this.givenName = givenName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setEmail(String email) { this.email = email; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setPhoneNumberVerified(Boolean phoneNumberVerified) { this.phoneNumberVerified = phoneNumberVerified; }
    public void setGender(String gender) { this.gender = gender; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    public void setZoneinfo(String zoneinfo) { this.zoneinfo = zoneinfo; }
    public void setLocale(String locale) { this.locale = locale; }
    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }
    public void setProfile(String profile) { this.profile = profile; }
    public void setPicture(String picture) { this.picture = picture; }
    public void setWebsite(String website) { this.website = website; }
    public void setAddress(Address address) { this.address = address; }
}
