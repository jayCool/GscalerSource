package algorithm;

import java.io.IOException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Option;

public class CommandParser {

    @Option(name = "-i", usage = "input of the graph file", metaVar = "INPUT")
    private static final String ORIGINFILE = "";

    @Option(name = "-o", usage = "ouput of the file", metaVar = "OUTPUT")
    private static final String OUTPUTDIR = "";

    @Option(name = "-sN", usage = "scaled node size", metaVar = "NODE SIZE")
    public static int scaledNodeSize;

    @Option(name = "-sE", usage = "scaled edge size", metaVar = "EDGE SIZE")
    public static int scaledEdgeSize;

    public CommandParser() {
    }

    public static void main(String[] args) throws IOException {
        CommandParser commandParser = new CommandParser();
        if (!commandParser.parseCmdLine(args)) {
            System.err.println("command wrong");
            System.exit(-1);
        } else {
            Gscaler gscaler = new Gscaler(scaledEdgeSize, scaledNodeSize, OUTPUTDIR, ORIGINFILE);
            gscaler.run();
        }
    }

    private boolean parseCmdLine(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
            if (CommandParser.ORIGINFILE.isEmpty() || CommandParser.OUTPUTDIR.isEmpty() || CommandParser.scaledNodeSize == 0 || CommandParser.scaledEdgeSize == 0) {
                System.out.println("java -jar Gscaler.jar -i inputfile -o outputfile -sE scaledEdgeSize -sN scaledNodeSize [options...] arguments...");
                System.out.println("  Example: java -jar Gscaler.jar" + parser.printExample(ALL));
                return false;
            }
        } catch (CmdLineException e) {
            System.out.println("java -jar Gscaler.jar -i inputfile -o outputfile -sE scaledEdgeSize -sN scaledNodeSize [options...] arguments...");
            System.out.println("  Example: java -jar Gscaler.jar" + parser.printExample(ALL));
            return false;
        }
        return true;
    }

}
