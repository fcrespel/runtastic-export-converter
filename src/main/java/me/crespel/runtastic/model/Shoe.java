package me.crespel.runtastic.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * Runtastic model for shoe (\User\Shoes).
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shoe {

	private String name;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
	private Date createdAt;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss Z")
    private Date updatedAt;
    private ShoeSize size;
    private String color;
    private Integer initialDistance;
    private List<ShoeMilestone> milestones;
    private List<ShoeStatistic> statistics;
    String id;
    String equipmentId;
    String equipmentType;
    List<String> samplesIds;

}