package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class StudyRepository(private val studyDao: StudyDao) {

    val allFolders: Flow<List<Folder>> = studyDao.getAllFolders()
    val allTasks: Flow<List<Task>> = studyDao.getAllTasks()
    val allTextbooks: Flow<List<Textbook>> = studyDao.getAllTextbooks()
    val allFlashcards: Flow<List<Flashcard>> = studyDao.getAllFlashcards()

    fun getTextbooksInFolder(folderId: Long): Flow<List<Textbook>> = studyDao.getTextbooksInFolder(folderId)
    fun getDrawingsInFolder(folderId: Long): Flow<List<Drawing>> = studyDao.getDrawingsInFolder(folderId)
    fun getTasksInFolder(folderId: Long): Flow<List<Task>> = studyDao.getTasksInFolder(folderId)
    fun getFlashcardsInFolder(folderId: Long): Flow<List<Flashcard>> = studyDao.getFlashcardsInFolder(folderId)

    suspend fun getDrawingById(id: Long): Drawing? = withContext(Dispatchers.IO) {
        studyDao.getDrawingById(id)
    }

    suspend fun insertFolder(name: String, icon: String = "📁"): Long = withContext(Dispatchers.IO) {
        studyDao.insertFolder(Folder(name = name, icon = icon))
    }

    suspend fun deleteFolder(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteFolder(id)
    }

    suspend fun insertTextbook(folderId: Long, title: String, author: String, content: String) = withContext(Dispatchers.IO) {
        studyDao.insertTextbook(Textbook(folderId = folderId, title = title, author = author, content = content))
    }

    suspend fun updateTextbook(textbook: Textbook) = withContext(Dispatchers.IO) {
        studyDao.updateTextbook(textbook)
    }

    suspend fun deleteTextbook(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteTextbook(id)
    }

    suspend fun insertDrawing(folderId: Long, name: String, backgroundType: String = "LINED"): Long = withContext(Dispatchers.IO) {
        studyDao.insertDrawing(Drawing(folderId = folderId, name = name, backgroundType = backgroundType))
    }

    suspend fun updateDrawing(drawing: Drawing) = withContext(Dispatchers.IO) {
        studyDao.updateDrawing(drawing)
    }

    suspend fun saveDrawingStrokes(drawingId: Long, strokesJson: String, backgroundType: String) = withContext(Dispatchers.IO) {
        studyDao.saveDrawingStrokes(drawingId, strokesJson, backgroundType)
    }

    suspend fun deleteDrawing(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteDrawing(id)
    }

    suspend fun insertTask(folderId: Long, title: String, priority: String = "MEDIUM"): Long = withContext(Dispatchers.IO) {
        studyDao.insertTask(Task(folderId = folderId, title = title, priority = priority))
    }

    suspend fun updateTaskStatus(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        studyDao.updateTaskStatus(id, isCompleted)
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteTask(id)
    }

    suspend fun insertFlashcard(folderId: Long, front: String, back: String) = withContext(Dispatchers.IO) {
        studyDao.insertFlashcard(Flashcard(folderId = folderId, front = front, back = back))
    }

    suspend fun deleteFlashcard(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteFlashcard(id)
    }

    suspend fun preseedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingFolders = studyDao.getAllFolders().firstOrNull()
        if (existingFolders.isNullOrEmpty()) {
            // Seed folders
            val calcFolderId = studyDao.insertFolder(Folder(name = "Advanced Calculus", icon = "📊"))
            val quantumFolderId = studyDao.insertFolder(Folder(name = "Quantum Physics", icon = "⚛️"))
            val chemFolderId = studyDao.insertFolder(Folder(name = "Organic Chemistry", icon = "🧪"))
            val bioFolderId = studyDao.insertFolder(Folder(name = "Genetics & Biology", icon = "🧬"))

            // Seed textbooks
            studyDao.insertTextbook(Textbook(
                folderId = calcFolderId,
                title = "Calculus limits & Derivatives",
                author = "Dr. Rachel Evans",
                content = """
                    Section 1: Limits & Continuity
                    In mathematics, a limit is the value that a function (or sequence) 'approaches' as the input (or index) approaches some value. Limits are essential to calculus and mathematical analysis, and are used to define continuity, derivatives, and integrals.
                    
                    Formally, let f be a real-valued function. We say the limit of f(x) as x approaches c is L if, for every epsilon > 0, there exists a delta > 0 such that |f(x) - L| < epsilon whenever 0 < |x - c| < delta.
                    
                    Section 2: Rules of Differentiation
                    Derivatives represent the instantaneous rate of change of a quantity.
                    Here are the fundamental rules:
                    1. Power Rule: d/dx [x^n] = n * x^(n-1)
                    2. Sine Rule: d/dx [sin(x)] = cos(x)
                    3. Cosine Rule: d/dx [cos(x)] = -sin(x)
                    4. Exponential Rule: d/dx [e^x] = e^x
                    5. Product Rule: (f * g)' = f'g + fg'
                    6. Chain Rule: d/dx [f(g(x))] = f'(g(x)) * g'(x)
                    
                    Try drawing these formulas on the Stylus Notepad in lined/grid mode and query the AI Tutor to explain them or generate active-recall quiz cards instantly!
                """.trimIndent()
            ))

            studyDao.insertTextbook(Textbook(
                folderId = quantumFolderId,
                title = "Quantum Physics Foundations",
                author = "Prof. Alan Vance",
                content = """
                    Chapter 1: Wave-Particle Duality
                    Light and matter exhibit behaviors of both waves and particles. This is wave-particle duality. The De Broglie relation relates wavelength lambda of a particle to its momentum p through Planck's constant h:
                    lambda = h / p
                    
                    Chapter 2: Schrödinger's Equation
                    The Schrödinger equation is a linear partial differential equation that governs the wave-function of a quantum-mechanical system:
                    i * h_bar * d/dt |psi> = H |psi>
                    where H is the Hamiltonian operator representing energy.
                    The wave-function Psi encodes all measurable physical properties of the particle or system, such as probability density of position.
                    
                    Chapter 3: Quantum Superposition
                    A system can exist in multiple states simultaneously until a measurement is made. Schrödinger's cat illustrates this conceptual challenge at microscopic vs macroscopic limits.
                """.trimIndent()
            ))

            studyDao.insertTextbook(Textbook(
                folderId = chemFolderId,
                title = "Organic Mechanism Synthesis",
                author = "Dr. Sarah Stone",
                content = """
                    Chapter 1: Nucleophilic Substitution
                    In organic chemistry, nucleophilic substitution is a fundamental class of reactions in which an electron-rich chemical species (the nucleophile) replaces a leaving group on an electron-deficient carbon atom.
                    
                    There are two primary mechanisms:
                    1. SN2 Reaction (Substitution Nucleophilic Bimolecular):
                    - Happens via a single transition state.
                    - High rate for primary alkyl halides.
                    - Causes inversion of stereocenter (Walden inversion).
                    
                    2. SN1 Reaction (Substitution Nucleophilic Unimolecular):
                    - Happens via a multi-step carbocation intermediate.
                    - High rate for tertiary alkyl halides due to intermediate stability.
                    - Leads to racemic mixtures.
                """.trimIndent()
            ))

            studyDao.insertTextbook(Textbook(
                folderId = bioFolderId,
                title = "Genetics & Molecular Replication",
                author = "Dr. Maya Lin",
                content = """
                    Chapter 1: DNA Structure & Replication
                    DNA (Deoxyribonucleic acid) is a double-stranded helix held together by hydrogen bonds between complementary base pairs: Adenine (A) Pairs with Thymine (T), and Cytosine (C) Pairs with Guanine (G).
                    
                    Replication follows a semi-conservative model, catalyzed by several major enzymes:
                    - Helicase: Unwinds the double helix at replication forks.
                    - Primase: Lays down RNA primers for DNA Polymerase.
                    - DNA Polymerase III: Synthesizes DNA strands in the 5' to 3' direction.
                    - DNA Polymerase I: Replaces RNA primers with DNA nucleotides.
                    - Ligase: Joins Okazaki fragments of the lagging strand.
                """.trimIndent()
            ))

            // Seed a starter drawing for calculus
            studyDao.insertDrawing(Drawing(
                folderId = calcFolderId,
                name = "Limits Graph Drawing",
                backgroundType = "GRID"
            ))

            // Seed some active tasks
            studyDao.insertTask(Task(folderId = calcFolderId, title = "Review Limits Definition Sheet", priority = "HIGH"))
            studyDao.insertTask(Task(folderId = quantumFolderId, title = "Practice De Broglie Questions", priority = "MEDIUM"))
            studyDao.insertTask(Task(folderId = chemFolderId, title = "Draw SN1 vs SN2 transitions", priority = "HIGH"))

            // Seed custom starter flashcard
            studyDao.insertFlashcard(Flashcard(folderId = calcFolderId, front = "What is the derivative of e^x?", back = "d/dx [e^x] = e^x"))
            studyDao.insertFlashcard(Flashcard(folderId = quantumFolderId, front = "Write the De Broglie relation formula", back = "lambda = h / p"))
        }
    }
}
