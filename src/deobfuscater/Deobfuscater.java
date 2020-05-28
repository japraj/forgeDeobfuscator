package deobfuscater;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Deobfuscater {

	// I did not write many comments because I thought of this as a one-off project
	// and did not intend to share it. The main reason I am sharing this is to get
	// some practice with using Git!

	// The below is just formatting for the console output; it only serves to
	// organize the console outputs; look @ the main function :D
	public static final String CONSOLE_TAG = "DEOBFUSCATER: ";
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

	public static String getTime() {
		return "[" + TIME_FORMATTER.format(LocalDateTime.now()) + "] ";
	}

	public static void print(String message) {
		System.out.print(getTime() + CONSOLE_TAG + message);
	}

	public static void println(String message) {
		print(message + "\n");
	}

	public static void printCommands() {
		println("Commands: ");
		println("       1. Overwrite - overwrites all obfuscated text in the specified file");
		println("       2. Deobfuscate - produces a new deobfuscated .txt file; does not overwrite the input file");
		println("       3. MapEntry - maps obfuscated functions/fields/parameters (for the input 'field_110324_m', outputs 'icons')");
		println("       4. OverwriteDirectory - deobfuscates (by overwriting) every single .java file in the specified directory");
		println("       5. Help - provides a list of all commands");
		println("       6. Exit - exits the program");
		println("");
	}

	public static void main(String[] args) {

		println("Made by Infamous");
		println("");

		String line;
		String[] components;
		Scanner reader;

		Map<String, String> functions = new HashMap<String, String>();
		Map<String, String> fields = new HashMap<String, String>();
		Map<String, String> parameters = new HashMap<String, String>();

		// The below try catch block just loads the map files that store the
		// deobfuscated function/field/parameter names; it looks a bit verbose because
		// it is able to load from both an IDE and from within a .jar file
		try {
			String resource = "/methods.txt";
			InputStream iStream = Deobfuscater.class.getResourceAsStream("/resources" + resource);
			if (iStream == null)
				iStream = Deobfuscater.class.getResourceAsStream(resource);
			reader = new Scanner(iStream);
			while (reader.hasNext()) {
				line = reader.nextLine();
				if (!line.contains("func"))
					continue;
				components = line.split(",");
				functions.put(components[0], components[1]);
			}

			reader.close();
			resource = "/fields.txt";
			iStream = Deobfuscater.class.getResourceAsStream("/resources" + resource);
			if (iStream == null)
				iStream = Deobfuscater.class.getResourceAsStream(resource);
			reader = new Scanner(iStream);
			while (reader.hasNext()) {
				line = reader.nextLine();
				if (!line.contains("field"))
					continue;
				components = line.split(",");
				fields.put(components[0], components[1]);
			}

			reader.close();
			resource = "/params.txt";
			iStream = Deobfuscater.class.getResourceAsStream("/resources" + resource);
			if (iStream == null)
				iStream = Deobfuscater.class.getResourceAsStream(resource);
			reader = new Scanner(iStream);
			while (reader.hasNext()) {
				line = reader.nextLine();
				if (!line.contains("p_"))
					continue;
				components = line.split(",");
				parameters.put(components[0], components[1]);
			}

			reader.close();
			println("Successfully loaded deobfuscation Maps");
			println("");
		} catch (Exception e) {
			println("Failed to load deobfuscation Maps");
			println("");
			e.printStackTrace();
		}

		printCommands();
		Scanner consoleReader = new Scanner(System.in);
		String input, command, fileName;
		
		// An explanation of the logic: my goal was to find a robust & efficient method
		// of mapping entire directories of obfuscated minecraft forge mod source code.
		// In order to make it as efficient as possible, I looked for patterns in the
		// obfuscated names of functions and set up an algorithm accordingly. I am not
		// familiar with mapping algorithms (program that find & replace entire files)
		// but I think this is pretty efficient because the entire algorithm is tailored
		// specifically to minecraft; we only have to read any file a single time to
		// ensure that it is entirely deobfuscated :)


		// Main loop for commands:
		while (true) {
			println("Try 'Help' for a list of commands.");
			print("Enter a command: ");
			input = consoleReader.nextLine().toLowerCase().replaceAll("\\s", "");
			if (input.contains("exit"))
				break;
			println("");
			if (input.equals("overwritedirectory")) {
				println("Please enter the name of the directory/folder you would like to deobfuscate.");
				println("Example: 'decompiled/minecraftmod/' given that the folder 'decompiled' is in the same directory as the jar file.");
				println("Every .java file in the directory (and its sub-directories) will be overwritten.");
				println("This can result in the loss of data so it is advised that you keep a backup.");
				print("Enter input: ");
				input = consoleReader.nextLine().toLowerCase().replaceAll("\\s", "");
				println("");
				try {
					Files.walkFileTree(Paths.get(input), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
							if (attributes.isRegularFile() && filePath.getFileName().toString().endsWith(".java")) {
								try {
									File file = filePath.toFile();

									Scanner reader = new Scanner(file);

									StringBuilder output = new StringBuilder();
									String line, temporary, storage;
									int index;

									while (reader.hasNext()) {

										line = reader.nextLine();

										while (line.contains("func_")) {
											temporary = line.substring(line.indexOf("func_"));
											temporary = temporary.substring(0, temporary.indexOf("("));
											if (functions.get(temporary) == null) {
												println("Failed to map function '" + temporary + "' in line '" + line
														+ "' in " + filePath);
												break;
											} else {
												line = line.replace(temporary, functions.get(temporary));
											}
										}

										while (line.contains("p_")) {
											index = 0;
											temporary = line.substring(line.indexOf("p_"));
											index += temporary.indexOf("_") + 1;
											storage = temporary.substring(temporary.indexOf("_") + 1);
											index += storage.indexOf("_") + 1;
											storage = storage.substring(storage.indexOf("_") + 1);
											index += storage.indexOf("_") + 1;
											temporary = temporary.substring(0, index);
											if (parameters.get(temporary) == null) {
												println("Failed to map parameter '" + temporary + "' in line '" + line
														+ "' in " + filePath);
												break;
											} else {
												line = line.replaceAll(temporary, parameters.get(temporary));
											}
										}

										while (line.contains("field_")) {
											temporary = line.substring(line.indexOf("field_"));
											try {
												temporary = temporary.substring(0, 15);
												if (fields.get(temporary) == null) {
													temporary = temporary.substring(0, 14);
													if (fields.get(temporary) == null) {
														temporary = temporary.substring(0, 13);
														if (fields.get(temporary) == null)
															temporary = temporary.substring(0, 12);
													}
												}
											} catch (Exception e) {
												try {
													temporary = temporary.substring(0, 14);
													if (fields.get(temporary) == null) {
														temporary = temporary.substring(0, 13);
														if (fields.get(temporary) == null)
															temporary = temporary.substring(0, 12);
													}
												} catch (Exception e1) {
													try {
														temporary = temporary.substring(0, 13);
														if (fields.get(temporary) == null)
															temporary = temporary.substring(0, 12);
													} catch (Exception e2) {
														temporary = temporary.substring(0, 12);
													}
												}
											}
											if (fields.get(temporary) == null) {
												println("Failed to map field'" + temporary + "' in line '" + line
														+ "' in " + filePath);
												break;
											} else {
												line = line.replace(temporary, fields.get(temporary));
											}
										}

										output.append(line + '\n');
									}
									reader.close();

									PrintWriter writer = new PrintWriter(new FileWriter(file));
									writer.write(output.toString());
									writer.close();
									println("Deobfuscated " + filePath.getFileName());
								} catch (Exception e) {
									println("Failed to deobfuscate " + filePath.getFileName());
									e.printStackTrace();
								}
							}
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException e) {
							e.printStackTrace();
							System.out.println("Failed to visit " + file.getFileName());
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} else if (input.equals("help")) {
				printCommands();
			} else if (input.equals("overwrite") || input.equals("deobfuscate")) {
				command = input;
				println("Please enter the directory of the .txt file you would like to deobfuscate.");
				println("Example: 'obfuscated.txt' for a file that is in the same directory as the jar file.");
				if (command.contains("overwrite"))
					println("Note: all text in the file will be overwritten; this can result in the loss of data so it is advised that you keep a backup.");
				print("Enter input: ");
				input = consoleReader.nextLine().toLowerCase().replaceAll("\\s", "");
				println("");
				fileName = input;
				try {
					File obfuscated = new File(input);
					reader = new Scanner(obfuscated);

					StringBuilder output = new StringBuilder();
					String temporary, storage;
					int index;
					while (reader.hasNext()) {
						line = reader.nextLine();
						while (line.contains("func_")) {
							temporary = line.substring(line.indexOf("func_"));
							temporary = temporary.substring(0, temporary.indexOf("("));
							if (functions.get(temporary) == null) {
								println("Failed to map function '" + temporary + "' in line '" + line + "' in "
										+ fileName);
								break;
							} else {
								line = line.replace(temporary, functions.get(temporary));
							}
						}

						while (line.contains("p_")) {
							index = 0;
							temporary = line.substring(line.indexOf("p_"));
							index += temporary.indexOf("_") + 1;
							storage = temporary.substring(temporary.indexOf("_") + 1);
							index += storage.indexOf("_") + 1;
							storage = storage.substring(storage.indexOf("_") + 1);
							index += storage.indexOf("_") + 1;
							temporary = temporary.substring(0, index);
							if (parameters.get(temporary) == null) {
								println("Failed to map parameter '" + temporary + "' in line '" + line + "' in "
										+ fileName);
								break;
							} else {
								line = line.replaceAll(temporary, parameters.get(temporary));
							}
						}

						while (line.contains("field_")) {
							temporary = line.substring(line.indexOf("field_"));
							temporary = temporary.substring(0, 15);
							try {
								temporary = temporary.substring(0, 15);
								if (fields.get(temporary) == null) {
									temporary = temporary.substring(0, 14);
									if (fields.get(temporary) == null) {
										temporary = temporary.substring(0, 13);
										if (fields.get(temporary) == null)
											temporary = temporary.substring(0, 12);
									}
								}
							} catch (Exception e) {
								try {
									temporary = temporary.substring(0, 14);
									if (fields.get(temporary) == null) {
										temporary = temporary.substring(0, 13);
										if (fields.get(temporary) == null)
											temporary = temporary.substring(0, 12);
									}
								} catch (Exception e1) {
									try {
										temporary = temporary.substring(0, 13);
										if (fields.get(temporary) == null)
											temporary = temporary.substring(0, 12);
									} catch (Exception e2) {
										temporary = temporary.substring(0, 12);
									}
								}
							}
							if (fields.get(temporary) == null) {
								println("Failed to map field'" + temporary + "' in line '" + line + "' in " + fileName);
								break;
							} else {
								line = line.replace(temporary, fields.get(temporary));
							}
						}
						output.append(line + '\n');
					}
					reader.close();
					PrintWriter writer;
					if (command.contains("overwrite")) {
						writer = new PrintWriter(new FileWriter(obfuscated));
					} else {
						while (true) {
							println("Specify the directory of the file you would like to output the deobfuscated text to.");
							println("Note: If the specified file does not exist, it will be automatically generated.");
							print("Enter input: ");
							input = consoleReader.nextLine().toLowerCase().replaceAll("\\s", "");
							println("");
							try {
								File outputFile = new File(input);
								writer = new PrintWriter(new FileWriter(outputFile));
								break;
							} catch (Exception e) {
								println("Error: unable to find specified file.");
							}
						}
					}
					writer.write(output.toString());
					writer.close();
					if (command.contains("overwrite"))

						println("Successfully deobfuscated " + fileName);
					else
						println("Successfully deobfuscated " + fileName + " to " + input);
					println("");

				} catch (Exception e) {
					println("Error: " + e.fillInStackTrace().getMessage());
					println("");
				}
			} else if (input.contains("map")) {
				println("This command will map obfuscated function and field names for you.");
				println("Enter 'return' to return to the main branch; the only commands accepted in this loop are field and function names.");
				while (true) {
					print("Enter a field or function code: ");
					input = consoleReader.nextLine().toLowerCase().replaceAll("\\s", "");
					if (input.equals("return")) {

						println("");
						break;
					} else if (input.contains("field")) {
						try {
							println(fields.get(input));
						} catch (Exception e) {
							println("Error: invalid field name; try again.");
						}
					} else if (input.contains("func")) {
						try {
							println(functions.get(input));
						} catch (Exception e) {
							println("Error: invalid function name; try again.");
						}
					} else if (input.contains("p_")) {
						try {
							println(parameters.get(input));
						} catch (Exception e) {
							println("Error: invalid parameter name; try again.");
						}
					} else {
						println("Invalid input; please try again.");
					}
					println("");
				}
			}
		}
		consoleReader.close();
		System.exit(0);
	}

}
