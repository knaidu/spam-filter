Naive Bayesian Spam Filter

How to load the spam filter?

Click here to launch spam filter using Java Web Start.

If you do not have Java Web Start installed on your system then click here (http://www.cise.ufl.edu/~kmehrotr/filter/client.jar) 
to download the filter. Incase you download it then you may launch the same by executing following command 
(even the double click should work but some systems have issues in associating a jar file with the executor): 

java -jar client.jar
How to configure the spam filter?

1. The first step is to train the spam filter on a set of spam and ham(non-spam) messages. 
   This is done by clicking on the 'Start Training' button on Training Controls tab. 
2. Please give 1-2 minutes for this process to complete as the model is trained on a set of ~2000 messages. 
3. The training set size has been limited to a small number due to space constraints on the server.

4. Once the engine is trained on the given corpus, and all the features (with their respective spammy probability) 
   are extracted, test the filter on emails in the inbox. This is done by:

Selecting 'Inbox' tab.
Click 'Load Inbox' to load emails from inbox.
Click 'Classify' to segregate spam emails from ham(non-spam) emails.

The evaluation measures: Accuracy and Total cost ration at the bottom of the screen reflect the efficacy of the 
spam filter.

What is the project all about?

The ever increasing menace of spam is bringing down productivity. More than 70% of the email messages are spam, 
and it has become a challenge to separate such messages from the legitimate ones. We have developed a spam 
identification engine which employs naïve Bayesian classifier to identify spam. This probabilistic classifier 
was trained on TREC 2006, a corpus of known spam/legitimate messages and it takes into account a comprehensive 
set of phrasal and domain specific features (non phrasal features viz. email containing attachments, emails sent 
from .edu domain etc) that are arrived at by using standard dimensionality reduction algorithms. 
The cost of classifying a legitimate message as spam (false positive) far outweighs the cost of classifying spam 
as legitimate (false negative). This cost sensitivity was incorporated into the spam engine and we have achieved 
high precision and recall, thereby reducing the false positive rates.

Read entire description here (http://www.cise.ufl.edu/~kmehrotr/filter/SpamFilterProjectReport.pdf)
