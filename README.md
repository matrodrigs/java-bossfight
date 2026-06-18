# Java Bossfight

Jogo 2D de boss fight em Java 21 com LibGDX e LWJGL3.

## Como executar

1. Instale um JDK 21.
2. Execute:

```powershell
.\gradlew.bat run
```

## Controles

- `A` / `Seta esquerda`: mover para a esquerda
- `D` / `Seta direita`: mover para a direita
- `W` / `Espaço` / `Seta cima`: pular
- `F` / `Ctrl`: atirar
- `K` / `Shift`: dash
- `G` / `Alt`: ataque especial
- `Esc`: voltar ao menu

## Destaques atuais

- Menu inicial com navegação, feedback visual e sons procedurais.
- Sequência de entrada direta com `READY?` e `GO!` antes do início da luta.
- Jogador com dash, partículas, invencibilidade temporária, knockback, hitstop e ataque especial carregável.
- Boss floral com máquina de estados, avisos visuais, reação a dano e múltiplos ataques.
- UI com vida do jogador, vida do boss e barra de especial.
- Sem assets oficiais de Cuphead; a arte, a locução de knockout fornecida para o projeto e os efeitos procedurais são independentes.

## Organização

- `screens`: telas do jogo.
- `entities`: objetos do jogo, como jogador, boss, projéteis e hitboxes.
- `boss`: estados da máquina de estados do boss.
- `systems`: sistemas simples de colisão, projéteis, partículas, áudio, fundo e texto retrô.
- `Constants`: constantes globais no pacote raiz.
- `assets`: sprites, música e locução usados em runtime.
- `docs`: documentação complementar, incluindo licenças e origem dos assets.
