package com.bennyhuo.qcloud

import org.apache.commons.cli.*
import java.io.File

/**
 * Created by benny on 8/12/17.
 */
private val cliOptions: Options = Options()

private fun loadOptions() {
    cliOptions.addOption("f", "file", true, "File or Directory to upload. Default for current directory.")
    cliOptions.addOption("m", "mdfile", true, "Markdown File or Directory to update. Default for current directory.")
    cliOptions.addOption("c", "config", true, "Config File contains APP_ID/APP_SECRET_ID/APP_SECRET_KEY/BUCKET.")
    cliOptions.addOption("appId", true, "appId.")
    cliOptions.addOption("secretId", true, "secretId")
    cliOptions.addOption("secretKey", true, "secretKey")
    cliOptions.addOption("bucket", true, "bucketName")
    cliOptions.addOption("region", true, "region, eg. tj")

    cliOptions.addOption("h", "help", false, "Print usages.")
}

fun printUsage() {
    println(
            """
${MetaInfo.name}

        ${MetaInfo.desc}

        by ${MetaInfo.author}
        v${MetaInfo.version}

${cliOptions.options().map(Option::usage).sorted().joinToString("\n")}
"""
    )
}

private fun Options.options(): MutableList<Option> = ArrayList<Option>().apply { addAll(options as Collection<Option>) }

private fun Option.usage(): String {
    return String.format("        -%-8s\t%-10s\t%-10s\t  %-20s", opt, if (hasLongOpt()) "[$longOpt]" else "", "", description)
}

fun CommandLine.readHelpOption(): CommandLine {
    if (hasOption("h")) {
        printUsage()
        System.exit(0)
    }
    return this
}

private fun CommandLine.readOptions(): TaskOptions {
    val appInfo = getOptionValue("c")
            ?.let { AppInfo(it) }
            ?: run {

        val settings = File.createTempFile("qcloud_uploader", "config")
        settings.deleteOnExit()
        AppInfo(settings.absolutePath).apply {
            APP_ID = getOptionValue("appId")?.toLongOrNull() ?: throw IllegalArgumentException()
            APP_SECRET_ID = getOptionValue("secretId") ?: throw IllegalArgumentException()
            APP_SECRET_KEY = getOptionValue("secretKey") ?: throw IllegalArgumentException()
            BUCKET = getOptionValue("bucket") ?: throw IllegalArgumentException()
            REGION = getOptionValue("region") ?: throw IllegalArgumentException()
        }
    }
    return TaskOptions(File(getOptionValue("f") ?: "."), appInfo, File(getOptionValue("m")))
}

fun main(args: Array<String>) {
    try {
        loadOptions()
        val options = PosixParser().parse(cliOptions, args, false)
                .readHelpOption()
                .readOptions()
        val uploader = Uploader(options)
        uploader.upload()
        options.mdFile?.let {
            val updater = MdFileUpdater(it, uploader.uploadHistory)
            updater.update()
        }
    } catch (ex: Exception) {
        //System.err.println(ex.cause)
        printUsage()
        return
    }
}