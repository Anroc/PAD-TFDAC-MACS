# Mractical Multi-Authority Attribute-Based Encryption Scheme for Secure Cloud Storage Systems


## Outline (Proposal)

* **Abstract**
	* Summarize the work
	* what are the results?
* **Introduction**
	* Ruff problem describtion
	* How ABE can solve this
	* How ABE works
	* Short history summary of ABE
	* What is expected of this work
	* What are the targets
* **Related work**
	* Of paper did something similar to this
	* Comparison paper
	* Not ABE paper
* **Contribution of this work**
	* What are the targets
	* What makes this work stand out
	* What we can gain from this work
* **Background Bdrive**
	* How does rekeying in bdrive work currently?
	* What security targets are covered
	* What is adventagous 
	* Waht is disadventagous
* **Requirements**
	* Basic requirements applicable to secure cloud
	* Special requirements to bdrive
	* Special requirements to ABE
* **Secure Group Communication**
	* *Motto: A better approach*
	* Into to Secure Group communcation
	* Basic idea
	* **Group Key Management Proctol**
		* Basic scheme implementing SGC
		* Describe how it works
		* Weaknesses strength compared to basic idea
	* **Logical Key Hirachy**
		* Extension to GKMP
		* What does it better?
		* What is still bad?
	* **One-way function trees**
		* Extension to LKH
		* What is better?
		* What is worst?
	* **Comparison**
		* Compare the schemes based on the overhead to Bdrive scheme
		* Point out the essential differences
		* Draw a conclusion
		* Extract best candidate from the scheme to go to the next stage (ABE)
		* Point out stat even the simplest sharing scheme scales better then Bdrive does
* **An Intoduction to the field of Attribute based encryption**
	* *Going beyond: A better approach*
	* Introduction to ABE
	* Basic idea, how it differe from SGC
		* Not users own keys, attribute do
		* Key creation is shifted to init phase rather then on creation a new group
	* Basic setup of an ABE system
		* Attribute authority
		* Central server
		* Users
	* What are the basics?
		* Pairing
		* Ellipic curves
	* Secure against Quantum
	* Performance?
	* **Comparison SGC to ABE**
		* Argue why ABE fits better
		* Authentication Management
		* Authoritation management
		* Argue on the meta level
		* Keep requirements in mind and compare them to thouse
		* Might compare on performance
			* But make clear that ABE is more heavy then SGC
		* Draw conclusion to ABE as a promising technology
* **Attribute based encryption**
	* *Motto: On the jorney to the matching scheme*
	* Defining the history of ABE
	* cluster schemes in clusters
	* selecting best candidate
	* Implement thouse
	* compare them on performance, scalability and requirments
	* Draw conclusion
* **Multi authority attribute based encryption**
	* *Motto: Fitting the requirements*
	* Defining the history of MA-ABE
	* cluster schemes in clusters
	* selecting best candidate
	* Implement thouse
	* compare them on performance, scalability and requirments
	* Draw conclusion
	* **DAC-MACS family**
		* *Motto: State of the arts*
* **Evaluation**
	* Compare implemented schemes
		* Bdrive
		* SGC
		* ABE
		* MA-ABE
		* DAC-MACS
	* Draw conclusion -- define resutls
* **ABE in practice**
	* Implementing ABE into Bdrive
	* Evaluate how user use this
* **Conclusion**
* **Future work**



