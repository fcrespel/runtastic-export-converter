package me.crespel.runtastic.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * Runtastic model for images meta data (\Photos\Images-meta-data).
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImagesMetaData implements Comparable<ImagesMetaData> {

    private Date createdAt;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private Integer id;

    @Override
    public int compareTo(ImagesMetaData o) {
		if (o == null) {
			return 1;
		} else if (this.id == null) {
			return -1;
		} 
        return (this.id.compareTo(o.id));
    }

}
