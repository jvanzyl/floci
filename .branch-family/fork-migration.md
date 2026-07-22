# IceGuard Floci Fork Migration

This checklist is preparation only. It grants no delete, push, ruleset,
publication, or pull-request authority.

## Verified Current State

- `iceguard/floci` is a public fork of `jvanzyl/floci`.
- `iceguard/floci-az` is a direct fork of `floci-io/floci-az`.
- `iceguard/floci-gcp` is a direct fork of `floci-io/floci-gcp`.
- Local `origin` is `https://github.com/jvanzyl/floci.git`.
- Local `upstream` is `https://github.com/floci-io/floci.git`.
- Local aggregate `main` is 30 commits ahead of upstream at
  `67d885aaeacb60b68ca1e52f84d7e132c0e41e11`.
- The 15 story branches are clean and test-compile. The two temporary
  prerequisite branch refs were removed after their boundary tips were recorded
  in `manifest.json` and the verified cleanup bundle.

## Existing Recovery Evidence

- Pre-refresh local family bundle:
  `/private/tmp/floci-pr-family-before-upstream-refresh-20260722.bundle`
  (`4d2cae531bfb3d3239e06da8591388d287181186d76aa9fda71814590bbf5a62`).
- Current GitHub fork bundle:
  `/private/tmp/iceguard-floci-before-rebuild-20260722.bundle`
  (`1f43609ed05f9260415891c23727892c231d59787567ad3bb04d17fda79f3763`).

## Approval-Gated Cutover

1. The operator exports the complete current repository and organizational
   ruleset definitions to local JSON files and checksums them. Ruleset creation,
   modification, disabling, restoration, and deletion are permanently
   operator-owned. Codex may inspect an exported artifact and verify the final
   effective protections, but must never mutate a ruleset.
2. Re-verify both bundles and all local branch tips.
3. Obtain explicit approval to delete and recreate `iceguard/floci`.
4. Delete the current `iceguard/floci` repository.
5. Create `iceguard/floci` as a direct organizational fork of
   `floci-io/floci`, matching the Azure and GCP topology.
6. Rename the local `origin` remote to `personal` and add the recreated
   `iceguard/floci` as `origin`; leave `upstream` pointed at `floci-io/floci`.
7. Push aggregate local `main` first, before restoring the repository-level
   default-branch ruleset.
8. Push only the six direct-upstream story branches initially: 01, 02, 03, 05,
   06, and 08. Held stories remain local until their prerequisites merge; then
   rebase only their own commits onto current upstream main and push the clean
   result. The removed synthetic prerequisite refs are never recreated or
   pushed.
9. The operator restores or applies the repository and organizational
   rulesets. Codex only verifies default-branch deletion, non-fast-forward,
   linear-history, resolved-thread, rebase-only, and PR requirements after the
   operator reports that work complete.
10. Verify the direct parent, source, default branch, aggregate SHA, story
    branch tips, Actions workflow inventory, and absence of releases, secrets,
    and variables.

No PR is created during this migration. Before each later upstream PR, rebase
only that story's own commits onto the then-current `floci-io/floci:main` and
verify that its diff contains no local prerequisite commits.
