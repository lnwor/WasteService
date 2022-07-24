/* Generated by AN DISI Unibo */ 
package it.unibo.wasteservice

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Wasteservice ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "init"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				var Material = ""
				var Quantity = 0.0f
				var Accepted = false
		return { //this:ActionBasciFsm
				state("init") { //this:State
					action { //it:State
						println("Start")
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitch() )
				}	 
				state("idle") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t06",targetState="handleDeposit",cond=whenRequest("loadDeposit"))
				}	 
				state("handleDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("loadDeposit(MAT,QNT)"), Term.createTerm("loadDeposit(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								
												Material = payloadArg(0)
												Quantity = payloadArg(1).toFloat()
								request("storageAsk", "storageAsk($Material)" ,"storagemanager" )  
						}
					}
					 transition(edgeName="t17",targetState="handleStorageReply",cond=whenReply("storageAt"))
				}	 
				state("handleStorageReply") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("storageAt(MAT,QNT)"), Term.createTerm("storageAt(MAT,QNT)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  Quantity < payloadArg(1).toFloat()  
								 ){answer("loadDeposit", "loadaccept", "loadaccept(_)"   )  
								 Accepted = true  
								}
								else
								 {answer("loadDeposit", "loadrejected", "loadrejected(_)"   )  
								  Accepted = false  
								 }
						}
					}
					 transition( edgeName="goto",targetState="idle", cond=doswitchGuarded({ Accepted == false  
					}) )
					transition( edgeName="goto",targetState="moveTrolleyIndoor", cond=doswitchGuarded({! ( Accepted == false  
					) }) )
				}	 
				state("moveTrolleyIndoor") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyMove", "trolleyMove(indoor)" ,"trolley" )  
					}
					 transition(edgeName="t28",targetState="makeTrolleyCollect",cond=whenReply("trolleyDone"))
				}	 
				state("makeTrolleyCollect") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyCollect", "trolleyCollect($Material,$Quantity)" ,"trolley" )  
					}
					 transition(edgeName="t39",targetState="moveTrolleyDeposit",cond=whenReply("trolleyDone"))
				}	 
				state("moveTrolleyDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						forward("pickedUp", "pickedUp(_)" ,"wastetruck" ) 
						request("trolleyMove", "trolleyMove($Material)" ,"trolley" )  
					}
					 transition(edgeName="t410",targetState="makeTrolleyDeposit",cond=whenReply("trolleyDone"))
				}	 
				state("makeTrolleyDeposit") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyDeposit", "trolleyDeposit(_)" ,"trolley" )  
					}
					 transition(edgeName="t511",targetState="waitTrolleyDone",cond=whenRequest("loadDeposit"))
					transition(edgeName="t512",targetState="moveToHome",cond=whenReply("trolleyDone"))
				}	 
				state("waitTrolleyDone") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
					}
					 transition(edgeName="t613",targetState="moveToHome",cond=whenReply("trolleyDone"))
				}	 
				state("moveToHome") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						request("trolleyMove", "trolleyMove(home)" ,"trolley" )  
					}
					 transition(edgeName="t714",targetState="idle",cond=whenReply("trolleyDone"))
				}	 
			}
		}
}
