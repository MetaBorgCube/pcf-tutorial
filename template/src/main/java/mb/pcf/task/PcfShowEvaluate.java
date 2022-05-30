package mb.pcf.task;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.aterm.common.TermToString;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pcf.PcfClassLoaderResources;
import mb.pcf.PcfScope;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.ShowFeedback;

@PcfScope
public class PcfShowEvaluate implements TaskDef<PcfShowEvaluate.Args, CommandFeedback> {

	public static class Args implements Serializable {
		private static final long serialVersionUID = 1L;

		public final ResourceKey file;

		public Args(ResourceKey file) {
			this.file = file;
		}

		@Override
		public boolean equals(@Nullable Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			final Args args = (Args) o;
			return file.equals(args.file);
		}

		@Override
		public int hashCode() {
			return Objects.hash(file);
		}

		@Override
		public String toString() {
			return "Args{" + "file=" + file + '}';
		}
	}

	private final PcfClassLoaderResources classloaderResources;
	private final PcfAnalyze analyze;
	private final PcfEvaluateTask evaluateTask;

	@Inject
	public PcfShowEvaluate(PcfClassLoaderResources classloaderResources, PcfAnalyze analyze,
			PcfEvaluateTask evaluateTask) {
		this.classloaderResources = classloaderResources;
		this.analyze = analyze;
		this.evaluateTask = evaluateTask;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		context.require(classloaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
		final ResourceKey file = args.file;

		final Supplier<Result<IStrategoTerm, ?>> analyzedAstSupplier = analyze
				.createSupplier(new ConstraintAnalyzeTaskDef.Input(file, null)).map(analysisResult -> {
					return analysisResult.map(output -> output.result.analyzedAst);
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
