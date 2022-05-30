package mb.pcf.task;

import java.io.Serializable;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.aterm.common.TermToString;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.pcf.PcfClassLoaderResources;
import mb.pcf.PcfScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;

@PcfScope
public class PcfShowEvaluate implements TaskDef<PcfShowEvaluate.Args, CommandFeedback> {

	public static class Args implements Serializable {
		private static final long serialVersionUID = 1L;

		public final ResourcePath rootDirectory;
		public final ResourceKey file;

		public Args(ResourcePath rootDirectory, ResourceKey file) {
			this.rootDirectory = rootDirectory;
			this.file = file;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((rootDirectory == null) ? 0 : rootDirectory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Args other = (Args) obj;
			if (file == null) {
				if (other.file != null)
					return false;
			} else if (!file.equals(other.file))
				return false;
			if (rootDirectory == null) {
				if (other.rootDirectory != null)
					return false;
			} else if (!rootDirectory.equals(other.rootDirectory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Args [rootDirectory=" + rootDirectory + ", file=" + file + "]";
		}
	}

	private final PcfClassLoaderResources classloaderResources;
	private final PcfAnalyzeFile analyzeFile;
	private final PcfEvaluateTask evaluateTask;

	@Inject
	public PcfShowEvaluate(PcfClassLoaderResources classloaderResources, PcfAnalyzeFile analyzeFile,
			PcfEvaluateTask evaluateTask) {
		this.classloaderResources = classloaderResources;
		this.analyzeFile = analyzeFile;
		this.evaluateTask = evaluateTask;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		context.require(classloaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
		final ResourceKey file = args.file;

		final Supplier<Result<IStrategoTerm, ?>> analyzedAstSupplier = analyzeFile
				.createSupplier(new ConstraintAnalyzeFile.Input(args.rootDirectory, args.file)).map(analysisResult -> {
					return analysisResult.map(output -> output.ast);
				});

		return context.require(evaluateTask, analyzedAstSupplier).mapOrElse(
				ast -> CommandFeedback
						.of(ShowFeedback.showText(TermToString.toString(ast), "Beta-reduced '" + file + "'")),
				e -> CommandFeedback.ofTryExtractMessagesFrom(e, file));
	}

	@Override
	public String getId() {
		return getClass().getName();
	}
}
