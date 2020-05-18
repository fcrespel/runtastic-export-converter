package me.crespel.runtastic.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * Runtastic model for user (\\User\\user.json).
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    
    private String firstName;
    private String lastName;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private Date createdAt;
    private String beginWeek;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	private Date birthday;
    private String cityName;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private Date confirmedAt;
    private String currentSignInIp;
    private String email;
    private String fbProxiedEMail;
    private String fbUserId;
    private Boolean fitbitConnected;
    private String formatOfDate;
    private String formatOfTime;
    private String gender;
    private BigDecimal height;
    private String language;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private Date lastSignInAt;
    private String lastSignInIp;
    private String locale;
    private String login;
    private String timeZone;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
    private Date tosAcceptedAt;
    private Integer tosAcceptedVersion;
    private Integer unitType;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
    private Date updatedAt;
    private BigDecimal weight;

}