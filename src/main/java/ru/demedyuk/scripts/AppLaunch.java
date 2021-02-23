package ru.demedyuk.scripts;

public class AppLaunch {

    public static void main(String[] args) {
        CleanDesktopScript cleanDesktopScript = new CleanDesktopScript();

        if (args.length == 0)
            throw new IllegalArgumentException("First argument is required");

        cleanDesktopScript.run(args[0]);
    }
}
