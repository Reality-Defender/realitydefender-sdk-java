package ai.realitydefender.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for POST /api/v2/user-feedback. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFeedbackRequest {

  private final String requestId;
  private final String label;
  private final String feedbackCategory;
  private final String comment;

  /**
   * @param requestId media / detection request ID
   * @param label REAL, SYNTHETIC, MANIPULATED, UNKNOWN
   * @param feedbackCategory FALSE_POSITIVE, FALSE_NEGATIVE, CONFIRMATION, OTHER
   * @param comment optional note (may be null)
   */
  public UserFeedbackRequest(
      String requestId, String label, String feedbackCategory, String comment) {
    this.requestId = requestId;
    this.label = label;
    this.feedbackCategory = feedbackCategory;
    this.comment = comment;
  }

  @JsonProperty("requestId")
  public String getRequestId() {
    return requestId;
  }

  @JsonProperty("label")
  public String getLabel() {
    return label;
  }

  @JsonProperty("feedbackCategory")
  public String getFeedbackCategory() {
    return feedbackCategory;
  }

  @JsonProperty("comment")
  public String getComment() {
    return comment;
  }
}
