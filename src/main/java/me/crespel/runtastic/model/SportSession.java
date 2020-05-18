package me.crespel.runtastic.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.topografix.gpx._1._1.BoundsType;
import com.topografix.gpx._1._1.GpxType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"id", "sportTypeId", "startTime", "duration", "distance", "userEquipmentIds"})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportSession implements Comparable<SportSession> {

	private Date startTime;
	private Date endTime;
	private Date createdAt;
	private Date updatedAt;
	private Integer startTimeTimezoneOffset;
	private Integer endTimeTimezoneOffset;
	private Integer distance;
	private Integer duration;
	private Integer elevationGain;
	private Integer elevationLoss;
	private BigDecimal averageSpeed;
	private Integer calories;
	private BigDecimal longitude;
	private BigDecimal latitude;
	private BigDecimal maxSpeed;
	private Integer pauseDuration;
	private Integer durationPerKm;
	private BigDecimal temperature;
	private String notes;
	private Integer pulseAvg;
	private Integer pulseMax;
	private Boolean manual;
	private Boolean edited;
	private Boolean completed;
	private Boolean liveTrackingActive;
	private Boolean liveTrackingEnabled;
	private Boolean cheeringEnabled;
	private Boolean indoor;
	private Boolean altitudeRefined;
	private String id;
	private String weatherConditionId;
	private String surfaceId;
	private String subjectiveFeelingId;
	private String sportTypeId;
	private List<String> userEquipmentIds;

	@JsonIgnore
	private List<ElevationData> elevationData;

	@JsonIgnore
	private List<GpsData> gpsData;

	@JsonIgnore
	private GpxType gpx;

	@JsonIgnore
	private List<HeartRateData> heartRateData;

	@JsonIgnore
	private SportSessionAlbums SessionAlbum;

	@JsonIgnore
	private List<ImagesMetaData> images;

	@JsonIgnore
	private List<SportSession> overlapSessions;

	@JsonIgnore
	private BoundsType innerBound;
	@JsonIgnore
	private BoundsType outerBound;

	@JsonIgnore
	private User user;

	public Boolean contains( String filter ) {
		Boolean ret = false;
		if( filter != null) {
			if (getId().equals(filter)) {
				// id is equal to filter (keyword)
				ret = true;
			} else if ((getNotes() != null) && getNotes().contains(filter)) {
				// notes are available and contains the filter (keyword)
				ret = true;
			} else if (getUserEquipmentIds()!=null) {
				for( String equipmentid : getUserEquipmentIds() ) {
					if( equipmentid.equals(filter)) {
						// equipment id is equal to filter (keyword)
						ret = true;
						break;
					}
				}
			} else if( getImages()!=null ){
				for( ImagesMetaData image : getImages() ) {
					if( image.getId().toString().equals(filter) ) {
						// photo id is equal to filter (keyword)
						ret = true;
						break;
					} else if( (image.getDescription() != null) && image.getDescription().contains(filter)) {
						// photo description cotnains the filter (keyowrd)
						ret = true;
						break;
					}
				}
			}
		} else {
			// no filter set; retun "true"
			ret = true;
		}
		return ret;
	}

	@Override
	public int compareTo(SportSession o) {
		if (o == null) {
			return 1;
		} else if (this.startTime == null) {
			return -1;
		} else {
			int ret = this.startTime.compareTo(o.startTime);
			if (ret == 0) {
				ret = this.id.compareTo(o.id);
			}
			return ret;
		}
	}

}
