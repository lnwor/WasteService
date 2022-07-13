/* Generated by AN DISI Unibo */ 
package it.unibo.dep_init

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Dep_init ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "send"
	}
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("send") { //this:State
					action { //it:State
						
									var Material = if (kotlin.random.Random.nextFloat() > 0.5) "glass" else "plastic"
									var Quantity = kotlin.random.Random.nextInt(10, 30)	
						request("loadDeposit", "loadDeposit($Material,$Quantity)" ,"pro_dep_wasteservice" )  
					}
					 transition(edgeName="t012",targetState="secondSend",cond=whenReply("loadaccept"))
				}	 
				state("secondSend") { //this:State
					action { //it:State
						delay(1000) 
						
									var Material = if (kotlin.random.Random.nextFloat() > 0.5) "glass" else "plastic"
									var Quantity = kotlin.random.Random.nextInt(10, 30)	
						request("loadDeposit", "loadDeposit($Material,$Quantity)" ,"pro_dep_wasteservice" )  
					}
					 transition(edgeName="t013",targetState="fin",cond=whenReply("loadaccept"))
				}	 
				state("fin") { //this:State
					action { //it:State
					}
				}	 
			}
		}
}