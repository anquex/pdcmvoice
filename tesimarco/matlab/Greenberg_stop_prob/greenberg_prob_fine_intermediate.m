% Marco Bettiol - 586580
% Batch Size Estimate

% Compute, for each slot i, the probability to finish basic greenberg
% algorithm in that slot given a batch of size n.
%
% Results are in table FINE(log2(n)+1,i)


clear all
close all
clc

%% probabilitià di trasmissione decrescente 2^-i

c=10000;            % numero cicli
N=17;                % (log del numero di nodi) +1
slots=N+6;           % maggiore numero nodi in quando possono trasmettere 'dopo'
x_i=[0:slots-1]'   % indici slot da 0 a N-1
n_i=2.^x_i % nodi attesi per slot (doppia trasposizione solo per avere il vettore in colonna)
format long
p_i=1./n_i         % probabilità di trasmettere     assegnata allo slot i


%sostituisci gli n_i per il test



a=round(linspace(2^9,2^10,13));
a2=[2*a(1:end-1)];
n_i=[a(2:end) a2(2:end)]

% i  indice riga    -> taglia 2^(i-1)
% ii indice colonna -> slot: 1,2,...
FINE=zeros(length(n_i),slots);

for i=1:length(n_i)
    n=n_i(i); 
    for ii=2:slots
        p=1;
        %termina nello slot ii
        for t=2:ii-1
            p=p*( 1-( (1-p_i(t))^n+n*p_i(t)*(1-p_i(t))^(n-1) ) );
        end
        p=p*( (1-p_i(ii))^n+n*p_i(ii)*(1-p_i(ii))^(n-1) );
        FINE(i,ii-1)=p;
        %disp(['      ' int2str(ii) ' - ' num2str(p)]);
    end
    
end

% azzero elementi piccoli
for i=1:length(n_i)
    for ii=1:slots
        if (FINE(i,ii)<10^-12)
            FINE(i,ii)=0;
        end
    end
end

% run greenberg_prob_fine_latex to display latex output



close all;
hold all;
grid on;

set(gca, 'Box', 'off' ); % here gca means get current axis
axis([7 13.01 0 .45])

fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

ColorOrder = ColorSpiral(floor(length(n_i)/2)+2+1,1,0);
ColorOrder=ColorOrder(2:end-1-1,:);
set(gca,'ColorOrder',[ColorOrder;[1,0.7,0.4];ColorOrder]);
set(gca,'LineStyleOrder','-*|:|o')

for i=1:length(n_i)
    h{i}=plot([1:slots],FINE(i,:));
    set(h{i},'MarkerFaceColor',get(h{i},'Color'));
    set(h{i},'MarkerSize',5);
    set(h{i},'MarkerEdgeColor','k');
end

for i=1:floor(length(n_i)/2)
    set(h{i},'Marker','s');
end

set(h{ceil(length(n_i)/2)},'Marker','d');
for i=floor(length(n_i)/2)+2:length(n_i)
    set(h{i},'Marker','o');
end

for i=1:length(n_i)
    llegends{i}=['n=' int2str(n_i(i))];
end
legend(llegends);
%legend('Location','NorthEastOutside')
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 12);

hx=xlabel('slot','Interpreter','Latex');
hy=ylabel('stop probability','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);

set(gca,'XTick',6:1:16);
saveas(fh, 'greenberg-stop-distribution-intermediate-values', 'epsc');