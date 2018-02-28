/*
 * Author: Lygerakis Fotios
 * AM: 2012030101
 * 
 * Boulware-like agent
 * 
 * 
 */
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.session.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.EvaluatorDiscrete;

/**
 * This is your negotiation party.
 */
public class FotisParty extends AbstractNegotiationParty {

	private double e=0;
	private Bid lastReceivedBid = null;
	private Bid MaxBid;
	private AdditiveUtilitySpace myUtilSpace;
	private Bid MinBid;
	private List<Issue> issues;
	private Map<Issue,EvaluatorDiscrete> IssueEvalMap;
	private Map<Issue, Double> IssueWeightsMap;
	private Bid myLastBid;
	private Bid myCurrentBid;
	private SortedOutcomeSpace mySortedOutcomeSpace;
	private TimeLineInfo myTimeline;
	private Deadline myDeadLine;
	private double k;
    private double newDeadline;
	
	
	@Override
	public void init(AbstractUtilitySpace utilSpace, Deadline dl,TimeLineInfo tl, long randomSeed, AgentID agentId) {
		super.init(utilSpace, dl, tl, randomSeed, agentId);

		// if you need to initialize some variables, please initialize them
		// below
		try{
			myUtilSpace=(AdditiveUtilitySpace)utilSpace;
			mySortedOutcomeSpace= new SortedOutcomeSpace(myUtilSpace);

			e=0.02;
			k=0.05;
			newDeadline=0.;
			
			myTimeline=tl;
			myDeadLine=dl;
			MaxBid=myUtilSpace.getMaxUtilityBid();
			MinBid=myUtilSpace.getMinUtilityBid();
			myLastBid=MaxBid;
			myCurrentBid=MaxBid;
			issues=myUtilSpace.getDomain().getIssues();
			IssueEvalMap= null;
			for(int i =0; i<issues.size();i++){
				IssueDiscrete tmpIssue = (IssueDiscrete)issues.get(i);
				EvaluatorDiscrete tmpEvaluator = (EvaluatorDiscrete) myUtilSpace.getEvaluator(i);
				IssueEvalMap.put(tmpIssue,tmpEvaluator);
				IssueWeightsMap.put(tmpIssue,tmpEvaluator.getWeight());
			}
			
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Action myAction;
		if (lastReceivedBid == null) {
			myCurrentBid=InitialOffer();
			myAction = new Offer(getPartyId(), myCurrentBid);
		}
		else{
			myCurrentBid=calculateOffer();
			if(myUtilSpace.getUtility(lastReceivedBid)>=myUtilSpace.getUtility(myLastBid)){
				myAction= new Accept(this.getPartyId(),lastReceivedBid);
			}
			else
				myAction = new Offer(getPartyId(),myCurrentBid);
		}
		myLastBid=myCurrentBid;
		return myAction;
	}

	private Bid InitialOffer() {
		// TODO Auto-generated method stub
		return MaxBid;
	}


	private Bid calculateOffer() {
		// TODO Auto-generated method stub
		try{
			return mySortedOutcomeSpace.getBidNearUtility(getCurrentUtility()).getBid();
		}catch(Exception e){
			return generateRandomBid();
		}
	}
/**
 * This method	calculates the desirable Utility of a bid my agent wants currently
 * It is a hybrid Boulware algorithm.
 * In fact it is a series of Boulware-like bids
 * This algorithm aims to speed of agreement 
 * 
 */
	private double getCurrentUtility(){
	    double Utility = myUtilSpace.getUtility(MaxBid) ;
	    double F;
	    if(myTimeline.getTime() > 0.7){    //simple Boulware
    		 F =(double) k+(1.-k)*(double)Math.pow(( (double) Math.min(myTimeline.getCurrentTime(), myDeadLine.getValue()) 
    				 / myDeadLine.getValue()), (double) (1./e) );
	    }
	    else{
	        F =(double) k+(1.-k)*(double)Math.pow(( (double) Math.min(myTimeline.getCurrentTime(), getPseudoDeadline()) 
                    / getPseudoDeadline()), (double) (1./e) );
	    }
	    Utility = (double)((double)getUtility(MinBid)
                +(double)(1.-(double)F) * (double)( getUtility(MaxBid)-getUtility(MinBid) ));
	    
	    if(myTimeline.getTime() < 0.7 && (Utility<0.75) ){             //too soon to be good
	        Utility=0.75;
	    }
		 return Utility;
	}


	private double getPseudoDeadline() {
	    double currentTime = myTimeline.getCurrentTime();
	    if(currentTime<=newDeadline){
	        return newDeadline;
	    }
	    newDeadline=currentTime+(myDeadLine.getValue()/12);
        return newDeadline;
    }


    /**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action. Can be null.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		if (action instanceof Offer) {
			lastReceivedBid = ((Offer) action).getBid();
		}
		/**
		 * Update Bid List
		 */
		if (lastReceivedBid != null) {
			//opponent_bid_list.insertBid(sender, lastReceivedBid);
		}
	}



    public String getPartyID(){
		return "FotisParty";
	}
    
	@Override
	public String getDescription(){
        return "Hybrid Boulware";
    }

    public String getVersion() {
		return "1.0";
	}


	
}
