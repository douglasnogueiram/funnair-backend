package ai.spring.demo.ai.playground.data;

public class PaymentRequest {
	
	    private String cardId;
	    private Double amount;
		private String cardVerificationValue;
		
		
	    public String getCardId() {
			return cardId;
		}
		public void setCardId(String cardId) {
			this.cardId = cardId;
		}
		public Double getAmount() {
			return amount;
		}
		public void setAmount(Double amount) {
			this.amount = amount;
		}
		public String getCardVerificationValue() {
			return cardVerificationValue;
		}
		public void setCardVerificationValue(String cardVerificationValue) {
			this.cardVerificationValue = cardVerificationValue;
		}


	    // getters e setters
	}
