package de.itemis.mps.gradle.generate


import jetbrains.mps.make.MakeSession
import jetbrains.mps.make.facet.FacetRegistry
import jetbrains.mps.make.facet.IFacet
import jetbrains.mps.make.facet.ITarget
import jetbrains.mps.make.script.IScript
import jetbrains.mps.make.script.ScriptBuilder
import jetbrains.mps.messages.IMessage
import jetbrains.mps.messages.IMessageHandler
import jetbrains.mps.messages.MessageKind
import jetbrains.mps.project.Project
import jetbrains.mps.smodel.SLanguageHierarchy
import jetbrains.mps.smodel.language.LanguageRegistry
import jetbrains.mps.smodel.resources.ModelsToResources
import jetbrains.mps.smodel.runtime.MakeAspectDescriptor
import jetbrains.mps.tool.builder.make.BuildMakeService
import org.apache.logging.log4j.LogManager
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.mps.openapi.language.SLanguage
import org.jetbrains.mps.openapi.model.SModel

private val logger = LogManager.getLogger("de.itemis.mps.gradle.generate")
private val messageLogger = LogManager.getLogger("de.itemis.mps.gradle.generate.messages")

private val DEFAULT_FACETS = listOf(
        IFacet.Name("jetbrains.mps.lang.core.Generate"),
        IFacet.Name("jetbrains.mps.lang.core.TextGen"),
        IFacet.Name("jetbrains.mps.make.facets.Make"),
        IFacet.Name("jetbrains.mps.lang.makeup.Makeup"))

private class MsgHandler : IMessageHandler {
    override fun handle(msg: IMessage) {
        when (msg.kind) {
            MessageKind.INFORMATION -> messageLogger.info(msg.text, msg.exception)
            MessageKind.WARNING -> messageLogger.warn(msg.text, msg.exception)
            MessageKind.ERROR -> messageLogger.error(msg.text, msg.exception)
            null -> messageLogger.error(msg.text, msg.exception)
        }
    }

}

private fun createScript(proj: Project, models: List<SModel>): IScript {

    val allUsedLanguagesAR: AsyncPromise<Set<SLanguage>> = AsyncPromise()
    val registry = LanguageRegistry.getInstance(proj.repository)

    proj.modelAccess.runReadAction {
        val allDirectlyUsedLanguages = models.map { it.module }.distinct().flatMap { it.usedLanguages }.distinct()
        allUsedLanguagesAR.setResult(SLanguageHierarchy(registry, allDirectlyUsedLanguages).extended)
    }

    val allUsedLanguages = allUsedLanguagesAR.get()

    val facetRegistry = proj.getComponent(FacetRegistry::class.java)
    val scb = ScriptBuilder (facetRegistry)

    when {
        allUsedLanguages == null -> logger.error("failed to retrieve used languages")
        allUsedLanguages.isEmpty() -> logger.warn("no used language is given")
        else -> {
            scb.withFacetNames(allUsedLanguages
                    .mapNotNull { registry.getLanguage(it) }
                    .mapNotNull { it.getAspect(MakeAspectDescriptor::class.java) }
                    .flatMap { it.manifest.facets() }
                    .map { it.name }
            )


            scb.withFacetNames(allUsedLanguages
                    .flatMap { facetRegistry.getFacetsForLanguage(it.qualifiedName) }
                    .map { it.name }
            )
        }
    }

    // For some reason MPS doesn't explicitly stat that there is a dependency on Generate, TextGen and Make, so we have
    // to make sure they are always included in the set of facets even if for MPS there is no dependency on them.

    // todo: not sure if we really need the final target to be Make.make all the time. The code was taken fom #BuildMakeService.defaultMakeScript
    return scb.withFacetNames(DEFAULT_FACETS).withFinalTarget(ITarget.Name("jetbrains.mps.make.facets.Make.make")).toScript()
}

private fun makeModels(proj: Project, models: List<SModel>): Boolean {
    val session = MakeSession(proj, MsgHandler(), true)
    val res = ModelsToResources(models).resources().toList()
    val makeService = BuildMakeService()

    if (res.isEmpty()) {
        logger.warn("nothing to generate")
        return false
    }
    logger.info("starting generation")
    val future = makeService.make(session, res, createScript(proj, models))
    try {
        val result = future.get()
        logger.info("generation finished")
        return if (result.isSucessful) {
            logger.info("generation result: successful")
            true
        } else {
            logger.error("generation result: failed")
            logger.error(result)
            false
        }
    } catch (ex: Exception) {
        logger.error("failed to generate", ex)
    }
    return false
}


fun generateProject(parsed: GenerateArgs, project: Project): Boolean {
    val ftr = AsyncPromise<List<SModel>>()

    project.modelAccess.runReadAction {
        var modelsToGenerate = project.projectModels
        if (parsed.models.isNotEmpty()) {
            modelsToGenerate = modelsToGenerate.filter { parsed.models.contains(it.name.longName) }
        }
        ftr.setResult(modelsToGenerate.toList())
    }

    val modelsToGenerate = ftr.get()

    if (modelsToGenerate == null) {
        logger.error("failed to fetch modelsToGenerate")
        return false
    }

    return makeModels(project, modelsToGenerate)
}
