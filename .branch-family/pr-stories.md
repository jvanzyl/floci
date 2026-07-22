# Floci AWS Compatibility PR Stories

These local branches reorganize the compatibility history into reviewer-sized
provider behavior stories. They grant no push, publication, or pull-request
authority.

## Freshness Record

- Public project: Floci, established by the public README and upstream remote.
- Upstream: `floci-io/floci:main` at
  `b245632a2e6b87da8e264f77fbdf479193650e16`, tree
  `55bd85dc0c91aa945864ec035f8a8baaa2a392c8`.
- Aggregate integration branch: local `main` at
  `67d885aaeacb60b68ca1e52f84d7e132c0e41e11`, tree
  `f303ddce4101c7630bdcbcca4789c64583d62572`, exactly 30 commits ahead.
- All aggregate and story worktrees were clean after reconstruction.
- Existing GitHub PR state remains unchecked; all story text is local draft
  material and grants no publication authority.

## Submission Model

`iceguard/floci:main` is the aggregate integration branch. It intentionally
contains every parity commit and is not the base of any upstream PR.

Every submitted PR must target the current `floci-io/floci:main`. A story with
prerequisites remains local until those prerequisites merge upstream. Its own
commits are then rebased onto current upstream main before the branch is pushed.
No PR targets another story branch and no maintainer is asked to review a
stacked diff.

| PR | Story | Own commits | Required upstream stories | Current state |
| --- | --- | ---: | --- | --- |
| 01 | Build reusable portable container images | 2 | none | direct-upstream |
| 02 | Complete the EC2 VPC lifecycle | 2 | none | direct-upstream |
| 03 | Enforce Standard On-Demand vCPU quotas | 1 | none | direct-upstream |
| 04 | Preserve and execute EC2 guest boot inputs | 3 | 02 | held |
| 05 | Issue and consume rotating role credentials | 2 | none | direct-upstream |
| 06 | Establish resource-aware IAM policy evaluation | 2 | none | direct-upstream |
| 07 | Complete IAM role and instance-profile management | 2 | 06 | held |
| 08 | Complete the RDS control-plane and managed-secret lifecycle | 2 | none | direct-upstream |
| 09 | Enforce RDS IAM authorization | 2 | 05, 06, 08 | held |
| 10 | Authorize tagged secret and key resources | 2 | 06 | held |
| 11 | Authorize SSM targets without overgranting managed policies | 2 | 06 | held |
| 12 | Support tagged HTTPS load balancers end to end | 2 | 06 | held |
| 13 | Authorize the complete EC2 launch-template lifecycle | 2 | 06 | held |
| 14 | Authorize exact Auto Scaling group operations | 2 | 06 | held |
| 15 | Reconcile and roll back instance refreshes | 2 | 04, 14 | held |

Story 09 was compiled above a synthetic prerequisite boundary combining stories
05, 06, and 08. Story 15 was compiled above a boundary combining stories 04 and
14 plus their transitive prerequisites. The temporary branch refs were removed
after verification; boundary commits `f39ac7d40` and `10ca536bb4` remain in the
respective story ancestry and in the verified cleanup bundle. They delimit the
two story commits to rebase after the prerequisites merge upstream.

## 01 Build Reusable Portable Container Images

**Why:** Contributors and downstream tests need reproducible native images
without copying the build pipeline, while local development needs a JVM runtime
capable of hosting glibc-linked integrations.

**Approach:** Expose the native amd64/arm64 and compat publication graph through
`workflow_call`, leave registry authority with the caller, preserve immutable
digest provenance, and use an Ubuntu Java 25 runtime without changing the
established entrypoint or architecture behavior.

**Commit stack:**
- `9a5afd2ad` `ci: add reusable image publishing`
- `f76dd4ee3` `fix(docker): use glibc in JVM development image`

**Review contract:** Callers own triggers, package permissions, credentials,
and destination repositories; the reusable workflow returns immutable native
and compat references, and the JVM image retains its startup contract.

## 02 Complete The EC2 VPC Lifecycle

**Why:** VPC Query operations and deletion did not expose or clean up the AWS
owned lifecycle state expected by SDK callers.

**Approach:** Complete Query pagination, tag validation, attachment persistence,
and removal of only the default security group, main route table, and default
network ACL when their VPC is deleted.

**Commit stack:**
- `ac93f0d4a` `feat(ec2): complete VPC Query compatibility`
- `f68b49ad7` `fix(ec2): clean up VPC default resources`

**Review contract:** User-created resources are not silently cascaded and
unrelated VPC dependency semantics remain unchanged.

## 03 Enforce Standard On-Demand vCPU Quotas

**Why:** The Service Quotas API and EC2 admission need one consistent regional
Standard On-Demand vCPU limit.

**Approach:** Add the standard quota catalog and enforce its bucket atomically
for instance launch and start operations.

**Commit stack:**
- `265b1e4aa` `feat(ec2): model and enforce standard on-demand vCPU quota`

**Review contract:** Exact-limit requests succeed, over-limit requests preserve
instance state, and Spot or nonstandard quota classes are out of scope.

## 04 Preserve And Execute EC2 Guest Boot Inputs

**Why:** Guest startup must preserve the caller's bytes, select an
architecture-compatible immutable image, and execute valid compressed payloads.

**Approach:** Carry byte-exact user data through launch templates, Auto Scaling,
persistence, and metadata; pin arm64 image identity; decode gzip only at the
guest execution boundary.

**Commit stack:**
- `c2dd6872e` `fix(ec2): preserve exact user data bytes`
- `6b6809877` `feat(ec2): model immutable arm64 guest images`
- `0361651d1` `fix(ec2): execute gzip-compressed user data`

**Review contract:** Metadata remains byte-exact, plain and malformed payloads
keep their prior behavior, and image aliases resolve to immutable catalog data.

## 05 Issue And Consume Rotating Role Credentials

**Why:** Temporary role sessions need expiry, token, routing, persistence, and
rotation semantics, and role-backed guests must actually use those sessions.

**Approach:** Model expiring STS credentials, rotate and revoke IMDS sessions,
and stop placeholder environment credentials from shadowing IMDS in profiled
guests.

**Commit stack:**
- `7897aad6b` `fix(iam): model temporary credentials and IMDS rotation`
- `23c52f6e8` `fix(ec2): prefer instance profile credentials in guests`

**Review contract:** Long-lived credentials and no-profile guest defaults remain
compatible while expired or mismatched sessions are rejected.

## 06 Establish Resource-Aware IAM Policy Evaluation

**Why:** Policy simulation and runtime enforcement need the same complete
request context and exact resource tuples.

**Approach:** Preserve repeated simulation context values, implement set
operators correctly, and centralize form and JSON request resolution for
multi-resource enforcement.

**Commit stack:**
- `28c2d5320` `fix(iam): decode simulation context values`
- `11b4b4658` `fix(iam): establish runtime authorization request resolution`

**Review contract:** Service-specific resource resolution does not weaken
wildcard or condition matching globally.

## 07 Complete IAM Role And Instance-Profile Management

**Why:** Role policy limits and role/profile lifecycle calls need AWS-shaped
limits, tags, persistence, and exact-resource authorization.

**Approach:** Enforce aggregate inline-policy size atomically and complete role
and instance-profile create, tag, associate, remove, and delete behavior.

**Commit stack:**
- `8647f924c` `fix(iam): enforce inline role policy quota`
- `6f3b3caeb` `feat(iam): complete role and instance profile lifecycle`

**Review contract:** Failed limits, dependencies, or permissions preserve all
existing policies, associations, profiles, and roles.

## 08 Complete The RDS Control-Plane And Managed-Secret Lifecycle

**Why:** RDS configuration and managed master credentials must round-trip and
change together across create, modify, rollback, persistence, and deletion.

**Approach:** Preserve explicit RDS fields and regional tags, then bind managed
secret and KMS selection to the DB instance lifecycle.

**Commit stack:**
- `c0ae0de67` `fix(rds): preserve control-plane configuration round trips`
- `cbe1d9d41` `fix(rds): complete managed master-secret lifecycle`

**Review contract:** Control-plane failure cannot leave DB and secret state out
of sync, and no general Secrets Manager orchestration layer is introduced.

## 09 Enforce RDS IAM Authorization

**Why:** PostgreSQL IAM tokens and RDS control-plane calls must be bound to the
exact principal, user, region, and resource state.

**Approach:** Validate the PostgreSQL token at connection time and resolve
future or persisted RDS ARNs with request and stored resource tags.

**Commit stack:**
- `b2f1d8664` `fix(rds): enforce PostgreSQL IAM user authorization`
- `b340b1d1b` `fix(rds): authorize exact runtime resources`

**Review contract:** Password authentication remains intact and denied IAM
calls do not open connections or mutate RDS state.

## 10 Authorize Tagged Secret And Key Resources

**Why:** Secrets Manager and KMS requests need prospective or persisted resource
identity and tag context instead of wildcard authorization.

**Approach:** Resolve secret namespaces and stored ARNs, resolve key and alias
resources from JSON requests, and evaluate request tags, key tags, tag keys,
region, and paired alias/key permissions.

**Commit stack:**
- `8e0479529` `fix(secretsmanager): authorize prospective and persisted resources`
- `79dcde775` `fix(kms): authorize tagged key lifecycle operations`

**Review contract:** Wrong namespace, key, alias, tag, action, or token remains
denied without broadening the shared policy matcher.

## 11 Authorize SSM Targets Without Overgranting Managed Policies

**Why:** SendCommand must authorize every target and document, while the seeded
SSM managed policy must not grant unrelated administrator access.

**Approach:** Resolve EC2 and managed-instance targets plus AWS-owned or account
documents, include stored target tags, and replace only the permissive
AmazonSSMManagedInstanceCore stand-in.

**Commit stack:**
- `a18651b45` `fix(ssm): authorize target and document resources`
- `68ee278dc` `fix(iam): scope SSM managed instance policy`

**Review contract:** Every resource tuple must pass before command creation and
other seeded managed policies remain unchanged.

## 12 Support Tagged HTTPS Load Balancers End To End

**Why:** ELBv2 lifecycle authorization and HTTPS listeners must work through the
same public SDK and data-plane contract.

**Approach:** Resolve future and persisted tagged ELBv2 resources, preserve
tag-on-create semantics, terminate TLS with configured certificates, and
restore HTTPS listeners from persisted state.

**Commit stack:**
- `f0ec5e565` `fix(elbv2): authorize tagged resource lifecycle`
- `11e73278b` `feat(elbv2): terminate TLS at HTTPS listeners`

**Review contract:** HTTP listeners retain existing behavior, unauthorized
lifecycle calls do not mutate state, and no public certificate authority or
global TLS policy engine is added.

## 13 Authorize The Complete EC2 Launch-Template Lifecycle

**Why:** Launch-template creation, deletion, version creation, and default
version updates must authorize the exact template and its tags.

**Approach:** Resolve future or persisted launch-template ARNs by ID or name,
evaluate request or stored tags and region, and preserve CreateTags-on-create
semantics.

**Commit stack:**
- `d759e825a` `fix(ec2): authorize launch template lifecycle`
- `f40e993fb` `fix(ec2): authorize existing launch template updates`

**Review contract:** Wrong-tag or missing-permission calls add no versions,
change no default, and do not alter unrelated EC2 authorization.

## 14 Authorize Exact Auto Scaling Group Operations

**Why:** Group configuration, tags, policies, hooks, updates, and refresh starts
must authorize the persisted group ARN and tag state.

**Approach:** Resolve group resources for every supported mutation, expose
request tags, stored tags, tag keys, and requested region, and evaluate every
tagged group tuple.

**Commit stack:**
- `4180f317b` `fix(autoscaling): authorize resource policy and hook lifecycle`
- `c195dd9c9` `fix(autoscaling): authorize existing group updates and refreshes`

**Review contract:** Denied calls preserve complete group state and no global
IAM matching rule is loosened.

## 15 Reconcile And Roll Back Instance Refreshes

**Why:** Instance refresh must converge target membership and replacement state,
and AutoRollback must restore actual pre-refresh launch identity after partial
failure.

**Approach:** Persist replacement progress and restart state, reconcile target
ports and warmup, then implement idempotent rollback success and failure states
that preserve direct and mixed-instances launch configuration.

**Commit stack:**
- `626dbd1ac` `fix(autoscaling): reconcile targets and instance refreshes`
- `31d40045c` `fix(autoscaling): implement instance refresh auto rollback`

**Review contract:** Successful refresh remains successful, failed rollback is
visible and leak-free, restart resumes safely, and no alternative rollout
orchestrator is introduced.

## Verification Status

- The aggregate is exactly 30 commits ahead of the refreshed upstream base.
- Every story contains its documented own-commit count above either upstream or
  its recorded prerequisite boundary; the obsolete linear stack is gone.
- Synthetic prerequisite branch refs were removed after recording their tips.
- Story 01 adds `.github/workflows/build-images.yml`; neither the aggregate nor
  any reconstructed branch contains `fork-image.yml`.
- All aggregate, story, and prerequisite worktrees were clean after the final
  reconstruction.
- Every final story tip passes `./mvnw -q -DskipTests test`, which compiles main
  and test sources. Full and focused runtime suites were not rerun for this
  branch-family-only reconstruction.
- No remote, push, pull request, ruleset, or repository mutation has occurred.
