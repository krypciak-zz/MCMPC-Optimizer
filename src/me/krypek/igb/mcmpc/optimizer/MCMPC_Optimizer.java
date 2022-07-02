package me.krypek.igb.mcmpc.optimizer;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.krypek.freeargparser.ArgType;
import me.krypek.freeargparser.ParsedData;
import me.krypek.freeargparser.ParserBuilder;
import me.krypek.mc.datapackparser.Datapack;
import me.krypek.mc.datapackparser.Datapack.DatapackFunction;
import me.krypek.utils.Utils;

public class MCMPC_Optimizer {

	public static void main(String[] args) {
		//@f:off
		ParsedData pd = new ParserBuilder()
				.add("p", "path", true, false, ArgType.String, "Path to a MCMPC datapack. Example: ~/.minecraft/saves/MCMulator_v7/datapack/MCMPCv7")
				.parse(args);
		//@f:on
		final String path = pd.getString("p");
		Datapack oldDatapack = Datapack.getDatapackFromFolder(path);

		DatapackFunction[] newFunctions = new DatapackFunction[oldDatapack.functions.length];
		for (int i = 0; i < oldDatapack.functions.length; i++) {
			DatapackFunction oldFunc = oldDatapack.functions[i];
			newFunctions[i] = new DatapackFunction(oldFunc.submodule(), oldFunc.name(), optimizeFunction(oldFunc.contents()));
		}
		Datapack newDatapack = new Datapack("MCMPCv7O", oldDatapack.packFormat, "MCMPCv7 Optimized", newFunctions);
		newDatapack.parse("/home/krypek/Games/minecraft/instances/mcmulator/.minecraft/saves/MCMulator_v7/datapacks/");
	}

	private static boolean deleteTellraw;

	private static String optimizeFunction(String c) {
		Stream<String> stream = Stream.of(c.split("\n"));

		deleteTellraw = true;
		stream = stream.filter(str -> {
			if(str.startsWith("#stop deleting tellraw!!"))
				deleteTellraw = false;
			if(str.startsWith("#you can delete tellraws now"))
				deleteTellraw = true;

			return !(str.startsWith("#") || (deleteTellraw && str.contains("tellraw")));
		}).map(str -> {
			if(str.equals("function ram:write"))
				return "execute store result entity 0-0-0-0-0 Pos[0] double 1 run scoreboard players get ramcell mcm\n"
						+ "execute at 0-0-0-0-0 store result block ~ ~ ~ RecordItem.tag.a int 1 run scoreboard players get readerInput mcm";

			if(str.equals("function ram:read"))
				return "execute store result entity 0-0-0-0-0 Pos[0] double 1 run scoreboard players get ramcell mcm\n"
						+ "execute at 0-0-0-0-0 store result score readerOutput mcm run data get block ~ ~ ~ RecordItem.tag.a";

			return replaceAllEntitySelectors(str);
		});

		return stream.collect(Collectors.joining("\n"));
	}

	//@f:off
	private static String[][] ENTITY_SELECTORS = {
			{"@e[name=rr]",	"@e[name=rr,limit=1]",	"@e[name=pr]",	"@e[name=pr,limit=1]",	"@e[name=ir]",	"@e[name=ir,limit=1]",	"@e[name=screenDraw]",	"@e[name=screenDraw,limit=1]",	"@e[name=resize]",	"@e[name=resize,limit=1]"},
			{"0-0-0-0-0",	"0-0-0-0-0",			"0-0-0-0-1",	"0-0-0-0-1",			"0-0-0-0-2",	"0-0-0-0-2",			"0-0-0-0-3",			"0-0-0-0-3",					"0-0-0-0-4",		"0-0-0-0-4"}
			};
	//@f:on

	private static String replaceAllEntitySelectors(String str) {
		str = Utils.stringReplaceAll(str, ENTITY_SELECTORS[0], ENTITY_SELECTORS[1]);
		return str;
	}
}
