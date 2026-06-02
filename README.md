# Java Bossfight

Base inicial de um jogo 2D de boss fight em Java 21 com LibGDX e LWJGL3.

## Como executar

1. Instale um JDK 21.
2. Execute:

```powershell
.\gradlew.bat run
```

## Controles

- `A` / `Seta esquerda`: mover para a esquerda
- `D` / `Seta direita`: mover para a direita
- `W` / `Espaco` / `Seta cima`: pular
- `J` / `Ctrl`: atirar
- `K` / `Shift`: dash
- `Esc`: voltar ao menu

## Organizacao

- `screens`: telas do jogo.
- `entities`: objetos do jogo, como jogador, boss, projeteis e hitboxes.
- `boss`: estados da maquina de estados do boss.
- `systems`: sistemas simples de colisao, projeteis, animacao e audio.
- `util`: constantes globais e wrapper de assets.
- `assets`: sprites, sons e fontes futuros.
