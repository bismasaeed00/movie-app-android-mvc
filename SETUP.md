# Session Setup

Please complete these three steps **before you start Task 1**. They take about a
minute and they matter — step 1 is what keeps your identity out of the study
data.

## 1. Set your participant identity for this repository

Run both commands from inside this project folder, replacing `P0XX` with the
participant ID printed on your handout:

```bash
git config user.name "P0XX"
git config user.email "P0XX@hs-heilbronn.de"
```

Check it worked:

```bash
git config user.name && git config user.email
```

This is set **for this repository only** — it does not change your global Git
settings or affect any of your own projects. Your personal name, email, and
GitHub account are never recorded.

## 2. Confirm you are on the baseline

```bash
git status
```

You should be on branch `main` with a clean working tree. If anything is already
modified, tell the researcher before continuing.

## 3. How to commit your work

Work directly on `main` — **you do not need to create branches.**

When you finish a task, make **one commit** whose message begins with the task
ID exactly as printed on your task sheet:

```bash
git add -A
git commit -m "TASK-X1: added genre filter"
```

**Do not push anywhere.** Everything is captured locally; there is no remote to
push to during the session.

If you later realise something in an earlier task needs fixing, that is
completely fine and expected — just begin that commit message with the
**original** task's ID so the work is counted against the right task:

```bash
git commit -m "TASK-X1: fix genre source"
```

Before you move on to the next task, the researcher will check the task off with
you.

## Questions during the session

Questions about the feature set, the acceptance criteria, or where things live
in the codebase will be answered directly — just ask.

Questions that amount to architectural judgment calls ("should this go in the
repository or the use case?") will be redirected back to the Orientation Guide
and the Codebase Map. That is not the researcher being unhelpful; making that
call yourself is part of what the study is measuring.
