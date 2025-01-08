package com.example.serving_web_content;

public class Badge {
	
	enum rank {
		
		INITIATE,
		BRONZE1,
		BRONZE2,
		BRONZE3,
		SILVER1,
		SILVER2,
		SILVER3,
		GOLD1,
		GOLD2,
		GOLD3,
		MASTER
	}
	
	rank badgeRank;
	
	public Badge() {
		
		badgeRank = rank.INITIATE;
	}
	
	public Badge(rank badgeRank) {
		
		this.badgeRank = badgeRank;
	}
	
	public void setBadgeRank(rank badgeRank) {
		
		this.badgeRank = badgeRank;
	}
	
	public void setRankFromString(String string) {
		
		switch(string) {
		
		case "INITIATE":
			
			badgeRank = rank.INITIATE;
			break;
			
		case "BRONZE1":
			
			badgeRank = rank.BRONZE1;
			break;
			
		case "BRONZE2":
			
			badgeRank = rank.BRONZE2;
			break;
			
		case "BRONZE3":
			
			badgeRank = rank.BRONZE3;
			break;
			
		case "SILVER1":
			
			badgeRank = rank.SILVER1;
			break;
			
		case "SILVER2":
			
			badgeRank = rank.SILVER2;
			break;
			
		case "SILVER3":
			
			badgeRank = rank.SILVER3;
			break;
			
		case "GOLD1":
			
			badgeRank = rank.GOLD1;
			break;
			
		case "GOLD2":
			
			badgeRank = rank.GOLD2;
			break;
			
		case "GOLD3":
			
			badgeRank = rank.GOLD3;
			break;
			
		case "MASTER":
			
			badgeRank = rank.MASTER;
			break;
		
		default:
			
			System.out.println("Oops! Something went wrong!");
			break;
		}
	}
	
	public rank getBadgeRank() {
		
		return badgeRank;
	}
	
	public void rankUp() {
		
		switch(badgeRank) {
		
			case INITIATE:
				
				badgeRank = rank.BRONZE1;
				break;
				
			case BRONZE1:
				
				badgeRank = rank.BRONZE2;
				break;
				
			case BRONZE2:
				
				badgeRank = rank.BRONZE3;
				break;
				
			case BRONZE3:
				
				badgeRank = rank.SILVER1;
				break;
				
			case SILVER1:
				
				badgeRank = rank.SILVER2;
				break;
				
			case SILVER2:
				
				badgeRank = rank.SILVER3;
				break;
				
			case SILVER3:
				
				badgeRank = rank.GOLD1;
				break;
				
			case GOLD1:
				
				badgeRank = rank.GOLD2;
				break;
				
			case GOLD2:
				
				badgeRank = rank.GOLD3;
				break;
				
			case GOLD3:
				
				badgeRank = rank.MASTER;
				break;
				
			case MASTER:
				
				break;
		}
	}
	
	@Override
	public String toString() {
		
		return badgeRank.toString();
	}
}

