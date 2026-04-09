package org.mitre.oidcprovider.infrastructure.adapter.out.persistence.document;

import org.mitre.oidcprovider.domain.model.UserInfo;
import org.mitre.oidcprovider.domain.model.vo.Address;
import org.mitre.oidcprovider.domain.model.vo.Subject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("user_info")
public class UserInfoDocument {

    public static class AddressDocument {
        private String formatted;
        private String streetAddress;
        private String locality;
        private String region;
        private String postalCode;
        private String country;

        public AddressDocument() {}

        public String getFormatted() { return formatted; }
        public void setFormatted(String formatted) { this.formatted = formatted; }
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public String getLocality() { return locality; }
        public void setLocality(String locality) { this.locality = locality; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    @Id
    private String id;

    private String sub;

    @Indexed(unique = true)
    private String preferredUsername;

    private String name;
    private String givenName;
    private String familyName;
    private String middleName;
    private String nickname;

    @Indexed
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
    private AddressDocument address;

    @Version
    private Long version;

    public UserInfoDocument() {}

    public static UserInfoDocument fromDomain(UserInfo userInfo) {
        UserInfoDocument doc = new UserInfoDocument();
        doc.id = userInfo.getSub().value();
        doc.sub = userInfo.getSub().value();
        doc.preferredUsername = userInfo.getPreferredUsername();
        doc.name = userInfo.getName();
        doc.givenName = userInfo.getGivenName();
        doc.familyName = userInfo.getFamilyName();
        doc.middleName = userInfo.getMiddleName();
        doc.nickname = userInfo.getNickname();
        doc.email = userInfo.getEmail();
        doc.emailVerified = userInfo.getEmailVerified();
        doc.phoneNumber = userInfo.getPhoneNumber();
        doc.phoneNumberVerified = userInfo.getPhoneNumberVerified();
        doc.gender = userInfo.getGender();
        doc.birthdate = userInfo.getBirthdate();
        doc.zoneinfo = userInfo.getZoneinfo();
        doc.locale = userInfo.getLocale();
        doc.updatedTime = userInfo.getUpdatedTime();
        doc.profile = userInfo.getProfile();
        doc.picture = userInfo.getPicture();
        doc.website = userInfo.getWebsite();
        if (userInfo.getAddress() != null) {
            Address addr = userInfo.getAddress();
            AddressDocument addrDoc = new AddressDocument();
            addrDoc.setFormatted(addr.formatted());
            addrDoc.setStreetAddress(addr.streetAddress());
            addrDoc.setLocality(addr.locality());
            addrDoc.setRegion(addr.region());
            addrDoc.setPostalCode(addr.postalCode());
            addrDoc.setCountry(addr.country());
            doc.address = addrDoc;
        }
        return doc;
    }

    public UserInfo toDomain() {
        UserInfo userInfo = UserInfo.create(new Subject(sub));
        userInfo.setPreferredUsername(preferredUsername);
        userInfo.setName(name);
        userInfo.setGivenName(givenName);
        userInfo.setFamilyName(familyName);
        userInfo.setMiddleName(middleName);
        userInfo.setNickname(nickname);
        userInfo.setEmail(email);
        userInfo.setEmailVerified(emailVerified);
        userInfo.setPhoneNumber(phoneNumber);
        userInfo.setPhoneNumberVerified(phoneNumberVerified);
        userInfo.setGender(gender);
        userInfo.setBirthdate(birthdate);
        userInfo.setZoneinfo(zoneinfo);
        userInfo.setLocale(locale);
        userInfo.setUpdatedTime(updatedTime);
        userInfo.setProfile(profile);
        userInfo.setPicture(picture);
        userInfo.setWebsite(website);
        if (address != null) {
            userInfo.setAddress(new Address(
                    address.getFormatted(),
                    address.getStreetAddress(),
                    address.getLocality(),
                    address.getRegion(),
                    address.getPostalCode(),
                    address.getCountry()
            ));
        }
        return userInfo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getPreferredUsername() { return preferredUsername; }
    public void setPreferredUsername(String preferredUsername) { this.preferredUsername = preferredUsername; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }
    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Boolean getPhoneNumberVerified() { return phoneNumberVerified; }
    public void setPhoneNumberVerified(Boolean phoneNumberVerified) { this.phoneNumberVerified = phoneNumberVerified; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    public String getZoneinfo() { return zoneinfo; }
    public void setZoneinfo(String zoneinfo) { this.zoneinfo = zoneinfo; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(String updatedTime) { this.updatedTime = updatedTime; }
    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }
    public String getPicture() { return picture; }
    public void setPicture(String picture) { this.picture = picture; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public AddressDocument getAddress() { return address; }
    public void setAddress(AddressDocument address) { this.address = address; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
