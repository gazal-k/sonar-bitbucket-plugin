package ch.mibex.bitbucket.sonar

import java.util.{List => JList}

import ch.mibex.bitbucket.sonar.cache.{InputFileCache, InputFileCacheSensor}
import ch.mibex.bitbucket.sonar.client.BitbucketClient
import ch.mibex.bitbucket.sonar.diff.IssuesOnChangedLinesFilter
import ch.mibex.bitbucket.sonar.review.{GitBaseDirResolver, PullRequestProjectBuilder, ReviewCommentsCreator, SonarReviewPostJob}
import org.sonar.api._
import org.sonar.api.rule.Severity

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer


object SonarBitbucketPlugin {
  final val BitbucketAccountName = "sonar.bitbucket.accountName"
  final val BitbucketRepoSlug = "sonar.bitbucket.repoSlug"
  final val BitbucketTeamName = "sonar.bitbucket.teamName"
  final val BitbucketApiKey = "sonar.bitbucket.apiKey"
  final val BitbucketBranchName = "sonar.bitbucket.branchName"
  final val SonarQubeIllegalBranchCharReplacement = "sonar.bitbucket.branchIllegalCharReplacement"
  final val SonarQubeMinSeverity = "sonar.bitbucket.minSeverity"
  final val BitbucketOAuthClientKey = "sonar.bitbucket.oauthClientKey"
  final val BitbucketOAuthClientSecret = "sonar.bitbucket.oauthClientSecret"
}


@Properties(
  // global = false: do not show these settings in the config page of SonarQube
  Array(
    new Property(
      key = SonarBitbucketPlugin.BitbucketAccountName,
      name = "Bitbucket account name",
      description = "The Bitbucket account your repository belongs to " +
        "(https://bitbucket.org/[account_name]/[repo_slug]).",
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketRepoSlug,
      name = "Bitbucket repo slug",
      description = "The slug of your Bitbucket repository (https://bitbucket.org/[account_name]/[repo_slug]).",
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketTeamName,
      name = "Bitbucket team name",
      description = "If you want to create pull request comments for Sonar issues under your team account, " +
        "provide the team name here.",
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketApiKey,
      name = "Bitbucket API key",
      description = "If you want to create pull request comments for Sonar issues under your team account, " +
        "provide the API key for your team account here.",
      `type` = PropertyType.PASSWORD,
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketOAuthClientKey,
      name = "Bitbucket OAuth client key",
      description = "If you want to create pull request comments for Sonar issues under your personal account " +
        "provide the client key of the OAuth consumer created for this application here (needs repository and " +
        "pull request WRITE permissions).",
      `type` = PropertyType.PASSWORD,
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketOAuthClientSecret,
      name = "Bitbucket OAuth client secret",
      description = "If you want to create pull request comments for Sonar issues under your personal account, " +
        "provide the OAuth client secret for this application here.",
      `type` = PropertyType.PASSWORD,
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.BitbucketBranchName,
      name = "Bitbucket branch name",
      description = "The branch name you want to get analyzed with SonarQube. When building with Jenkins, " +
        "use $GIT_BRANCH. For Bamboo, you can use ${bamboo.repository.git.branch}.",
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.SonarQubeIllegalBranchCharReplacement,
      name = "SonarQube invalid branch character replacement",
      description = "If you are using SonarQube version <= 4.5, then you have to escape '/' in your branch names " +
        "with another character. Please provide this replacement character here.",
      global = false
    ),
    new Property(
      key = SonarBitbucketPlugin.SonarQubeMinSeverity,
      name = "Min. severity to create pull request comments",
      defaultValue = Severity.MAJOR, // we cannot use default Sonar#defaultSeverity here as this is not a constant value
      description = "Use either INFO, MINOR, MAJOR, CRITICAL or BLOCKER to only have pull request comments " +
        "created for issues with severities greater or equal to this one.",
      global = false
    )
  )
)
class SonarBitbucketPlugin extends SonarPlugin {

  override def getExtensions: JList[Object] = {
    ListBuffer(
      classOf[SonarReviewPostJob],
      classOf[PluginConfiguration],
      classOf[PullRequestProjectBuilder],
      classOf[BitbucketClient],
      classOf[InputFileCacheSensor],
      classOf[ReviewCommentsCreator],
      classOf[IssuesOnChangedLinesFilter],
      classOf[GitBaseDirResolver],
      classOf[InputFileCache]
    ).toList
  }

}
