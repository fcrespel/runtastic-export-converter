package me.crespel.runtastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * Runtastic model for shoe milestone, used within shoe (\User\Shoes).
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoeMilestone {
    
    private Integer value;
    private String type;

}