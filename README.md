This tool implements the algorithm of GScaler Algorithm published in EDBT2016.

https://openproceedings.org/2016/conf/edbt/paper-68.pdf
 

To run the command:

java -jar Gscaler.jar -i inputfile -o outputfile -sE scaledEdgeSize -sN scaledNodeSize

For example, your original graph edge list is myGraph.txt.
You want to scale to a graph with 100 nodes and 200 edges, and output to myScaling.txt

Then, the command is 

java -jar Gscaler.jar -i myGraph.txt -o myScaling.txt -sE 200 -sN 100


Users can use GscalerCloud-the online service of Gscaler at

http://scaler.d2.comp.nus.edu.sg/GscalerCloud/



Please feel free to contact a0054808@u.nus.edu.
