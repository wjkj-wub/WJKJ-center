# WJwebsite online
------------日常工作管理-----------
git add --files
------提交到缓存区
    git diff --files(查看更改内容)
    git log     --files (查看历史记录)
git commit -m "somethings"
------提交代码到本地代码库
    git reset   -hard   HEAD^
    ------回退到上一个版本，HEAD~100————前100个版本。

------------与线上GitHub进行关联管理-----------

git  remote add origin git@github.com:wjkj-wub/wjcenter.git
------本地代码库与线上进行关联

------生成ssh密钥
ssh-keygen -t rsa -C "email@email.com"

------拉取线上库
git pull origin master --allow-unrelated-histories

------推送本地库至线上库
git push origin master 

-------------------git bisect 查BUG
# 开始 bisect
$ git bisect start
 
# 录入正确的 commit
$ git bisect good xxxxxx
 
# 录入出错的 commit
$ git bisect bad xxxxxx
 
# 然后 git 开始在出错的 commit 与正确的 commit 之间开始二分查找，这个过程中你需要不断的验证你的应用是否正常
$ git bisect bad
$ git bisect good
$ git bisect good
...
 
# 直到定位到出错的 commit，退出 bisect
$ git bisect reset


-------------------git 分支学习

查看分支：git branch
创建分支：git branch <name>
切换分支：git checkout <name>
创建+切换分支：git checkout -b <name>
合并某分支到当前分支：git merge <name>
删除分支：git branch -d <name> 
小结：分支不是文件夹，而是当前库的头部文件。即所有人拉取代码时同步的头部文件。从远程库克隆时自动关联。
----临时储存代码
临时储存当前代码:git stash 
切换master--建立新分支--修复bug--合并分支--删除bug分支--回到dev分支--
查看临时储藏：git stash lis 
恢复临时储藏：git stash apply
删除临时储藏库：git stash drop
-----解决冲突
git merge (分支)---合并修改到分支
$git status 查询冲突文件
    On branch master
    Your branch is ahead of 'origin/master' by 2 commits.
    (use "git push" to publish your local commits)

    You have unmerged paths.
    (fix conflicts and run "git commit")
    (use "git merge --abort" to abort the merge)

    Unmerged paths:
    (use "git add <file>..." to mark resolution)

    both modified:   readme.txt

    no changes added to commit (use "git add" and/or "git commit -a")

-------------------git rebase变基
git log --graph --pretty=oneline --abbrev-commit 查看分支提交历史



------------------------常用命令

   mkdir：         XX (创建一个空目录 XX指目录名)
   pwd：          显示当前目录的路径。
   git init          把当前的目录变成可以管理的git仓库，生成隐藏.git文件。
   git add XX       把xx文件添加到暂存区去。
   git commit –m “XX”  提交文件 –m 后面的是注释。
   git status        查看仓库状态
   git diff  XX      查看XX文件修改了那些内容
   git log          查看历史记录
   git reset  –hard HEAD^ 或者 git reset  –hard HEAD~ 回退到上一个版本
                        (如果想回退到100个版本，使用git reset –hard HEAD~100 )
   cat XX         查看XX文件内容
   git reflog       查看历史记录的版本号id
   git checkout — XX  把XX文件在工作区的修改全部撤销。
   git rm XX          删除XX文件
   git remote add origin https://github.com/tugenhua0707/testgit 关联一个远程库
   git push –u(第一次要用-u 以后不需要) origin master 把当前master分支推送到远程库
   git clone https://github.com/tugenhua0707/testgit  从远程库中克隆
   git checkout –b dev  创建dev分支 并切换到dev分支上
   git branch  查看当前所有的分支
   git checkout master 切换回master分支
   git merge dev    在当前的分支上合并dev分支
   git branch –d dev 删除dev分支
   git branch name  创建分支
   git stash 把当前的工作隐藏起来 等以后恢复现场后继续工作
   git stash list 查看所有被隐藏的文件列表
   git stash apply 恢复被隐藏的文件，但是内容不删除
   git stash drop 删除文件
   git stash pop 恢复文件的同时 也删除文件
   git remote 查看远程库的信息
   git remote –v 查看远程库的详细信息
   git push origin master  Git会把master分支推送到远程库对应的远程分支上



-------------------git 标签管理

标签是给分支设立的,默认标签是打在最新提交的commit上的。
git tag v1.0........建立标签

git tag    查看标签

给历史提交的commit打标签

 git log --pretty=oneline --abbrev-commit
 从结果的树上取前7位*******

 git tag v0.9 *******

git tag -a v0.1 -m "说明文字"  *******
-----------------
 git show v0.1   查看说明文字

 git tag -d v0.1   删除v0.1的本地标签
 git push origin :refs/tags/v0.1   删除v0.1的远程标签

 git push origin （v1.0；--tags)   推送标签至远程(1.0版本或所有标签)

------------------------------
忽略特殊文件或目录
Ignore files.gitignore